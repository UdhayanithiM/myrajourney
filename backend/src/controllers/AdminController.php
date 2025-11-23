<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Utils\Response;
use Src\Models\UserModel;
use Src\Config\DB;

class AdminController
{
    private UserModel $users;

    public function __construct()
    {
       $this->users = new UserModel();
    }

    public function createUser(): void
    {
       $auth = $_SERVER['auth'] ?? [];
       $role = $auth['role'] ?? '';
       $creatorId = (int)($auth['uid'] ?? 0);

       // ✅ CHANGE 1: Allow ADMIN and DOCTOR roles
       if ($role !== 'ADMIN' && $role !== 'DOCTOR') {
          Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied']], 403);
          return;
       }

       $body = json_decode(file_get_contents('php://input'), true) ?? [];

       $email = trim(strtolower($body['email'] ?? ''));
       $password = (string)($body['password'] ?? '');
       $userRole = in_array($body['role'] ?? 'PATIENT', ['PATIENT','DOCTOR']) ? $body['role'] : 'PATIENT';
       $name = $body['name'] ?? null;
       $phone = $body['phone'] ?? $body['mobile'] ?? null;

       // ✅ CHANGE 2: Doctors can ONLY create Patients
       if ($role === 'DOCTOR' && $userRole !== 'PATIENT') {
           Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Doctors can only register new patients.']], 403);
           return;
       }

       if (!$email || strlen($password) < 6) {
          Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Invalid email or password (min 6 chars)']], 422);
          return;
       }

       if ($this->users->findByEmail($email)) {
          Response::json(['success'=>false,'error'=>['code'=>'EMAIL_TAKEN','message'=>'Email already registered']], 409);
          return;
       }

       $db = DB::conn();
       try {
           $db->beginTransaction();

           $uid = $this->users->create([
              'email'=>$email,
              'password_hash'=>password_hash($password, PASSWORD_BCRYPT),
              'role'=>$userRole,
              'name'=>$name,
              'phone'=>$phone,
           ]);

           if ($userRole === 'PATIENT') {
              // ✅ CHANGE 3: Auto-assign to the creating Doctor
              if ($role === 'DOCTOR') {
                  $assignedDoctorId = $creatorId;
              } else {
                  // Admins can optionally assign a doctor ID via the request
                  $assignedDoctorId = isset($body['assigned_doctor_id']) ? (int)$body['assigned_doctor_id'] : null;
              }

              $address = $body['address'] ?? null;
              $age = $body['age'] ?? null;
              $gender = $body['gender'] ?? null;

              $stmt = $db->prepare('INSERT INTO patients (id, assigned_doctor_id, address, age, gender, created_at, updated_at) VALUES (:id, :doctor_id, :address, :age, :gender, NOW(), NOW())');
              $stmt->execute([
                  ':id' => $uid,
                  ':doctor_id' => $assignedDoctorId,
                  ':address' => $address,
                  ':age' => $age,
                  ':gender' => $gender
              ]);
           }

           if ($userRole === 'DOCTOR') {
              $stmt = $db->prepare('INSERT INTO doctors (id, specialization, created_at, updated_at) VALUES (:id, :specialization, NOW(), NOW())');
              $stmt->execute([
                  ':id' => $uid,
                  ':specialization' => $body['specialization'] ?? null
              ]);
           }

           $db->commit();

           Response::json(['success'=>true,'data'=>[
              'id'=>$uid,
              'email'=>$email,
              'role'=>$userRole,
              'name'=>$name,
           ]], 201);

       } catch (\Throwable $e) {
           $db->rollBack();
           error_log("Create User Failed: " . $e->getMessage());
           Response::json([
               'success'=>false,
               'error'=>[
                   'code'=>'CREATION_FAILED',
                   'message'=>'Failed to create user. ' . $e->getMessage()
               ]
           ], 500);
       }
    }

    public function listUsers(): void
    {
       $auth = $_SERVER['auth'] ?? [];
       $role = $auth['role'] ?? '';

       if ($role !== 'ADMIN') {
          Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied']], 403);
          return;
       }

       $db = DB::conn();
       $stmt = $db->prepare("
           SELECT
               u.id, u.name, u.email, u.role, u.phone,
               p.age, p.gender, p.address, p.assigned_doctor_id,
               d.specialization
           FROM users u
           LEFT JOIN patients p ON u.id = p.id
           LEFT JOIN doctors d ON u.id = d.id
           ORDER BY u.created_at DESC
       ");
       $stmt->execute();
       $users = $stmt->fetchAll();

       Response::json(['success'=>true,'data'=>$users]);
    }

    public function assignPatientToDoctor(): void
    {
       $auth = $_SERVER['auth'] ?? [];
       $role = $auth['role'] ?? '';

       if ($role !== 'ADMIN') {
          Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Only admins can assign patients']], 403);
          return;
       }

       $body = json_decode(file_get_contents('php://input'), true) ?? [];
       $patientId = (int)($body['patient_id'] ?? 0);
       $doctorId = isset($body['doctor_id']) ? (int)$body['doctor_id'] : null;

       if ($patientId <= 0) {
          Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Invalid patient ID']], 422);
          return;
       }

       $db = DB::conn();

       $stmt = $db->prepare('SELECT id FROM users WHERE id = :id AND role = "PATIENT"');
       $stmt->execute([':id'=>$patientId]);
       if (!$stmt->fetch()) {
          Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Patient not found']], 404);
          return;
       }

       if ($doctorId !== null) {
          $stmt = $db->prepare('SELECT id FROM users WHERE id = :id AND role = "DOCTOR"');
          $stmt->execute([':id'=>$doctorId]);
          if (!$stmt->fetch()) {
             Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Doctor not found']], 404);
             return;
          }
       }

       $stmt = $db->prepare('UPDATE patients SET assigned_doctor_id = :doctor_id, updated_at = NOW() WHERE id = :patient_id');
       $stmt->execute([':doctor_id'=>$doctorId, ':patient_id'=>$patientId]);

       Response::json(['success'=>true,'message'=>'Patient assigned successfully']);
    }

    public function listDoctors(): void
    {
       $auth = $_SERVER['auth'] ?? [];
       $role = $auth['role'] ?? '';

       if ($role !== 'ADMIN') {
          Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied']], 403);
          return;
       }

       $db = DB::conn();
       $stmt = $db->prepare("SELECT u.id, u.name, u.email, d.specialization
          FROM users u
          LEFT JOIN doctors d ON u.id = d.id
          WHERE u.role = 'DOCTOR' AND u.status = 'ACTIVE'
          ORDER BY u.name ASC");
       $stmt->execute();
       $doctors = $stmt->fetchAll();

       Response::json(['success'=>true,'data'=>$doctors]);
    }
}