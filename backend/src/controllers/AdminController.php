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

    // Create user (patient/doctor) - only admins can do this
    public function createUser(): void
    {
       $auth = $_SERVER['auth'] ?? [];
       $role = $auth['role'] ?? '';

       if ($role !== 'ADMIN') {
          Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Only admins can create users']], 403);
          return;
       }

       $body = json_decode(file_get_contents('php://input'), true) ?? [];

       // Clean input
       $email = trim(strtolower($body['email'] ?? ''));
       $password = (string)($body['password'] ?? '');
       $userRole = in_array($body['role'] ?? 'PATIENT', ['PATIENT','DOCTOR']) ? $body['role'] : 'PATIENT';
       $name = $body['name'] ?? null;

       // ✅ FIXED: Check both 'phone' AND 'mobile' keys (Android uses 'mobile')
       $phone = $body['phone'] ?? $body['mobile'] ?? null;

       // Validation
       if (!$email || strlen($password) < 6) {
          Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Invalid email or password (min 6 chars)']], 422);
          return;
       }

       // Check unique email
       if ($this->users->findByEmail($email)) {
          Response::json(['success'=>false,'error'=>['code'=>'EMAIL_TAKEN','message'=>'Email already registered']], 409);
          return;
       }

       // --- START TRANSACTION ---
       $db = DB::conn();
       try {
           $db->beginTransaction();

           // 1. Create Base User
           $uid = $this->users->create([
              'email'=>$email,
              'password_hash'=>password_hash($password, PASSWORD_BCRYPT),
              'role'=>$userRole,
              'name'=>$name,
              'phone'=>$phone,
           ]);

           // 2. Create Role Specific Profile
           if ($userRole === 'PATIENT') {
              $assignedDoctorId = isset($body['assigned_doctor_id']) ? (int)$body['assigned_doctor_id'] : null;

              // ✅ FIXED: Ensure address is captured
              $address = $body['address'] ?? null;

              // Insert into patients table
              $stmt = $db->prepare('INSERT INTO patients (id, assigned_doctor_id, address, created_at, updated_at) VALUES (:id, :doctor_id, :address, NOW(), NOW())');
              $stmt->execute([
                  ':id' => $uid,
                  ':doctor_id' => $assignedDoctorId,
                  ':address' => $address
              ]);
           }

           if ($userRole === 'DOCTOR') {
              $stmt = $db->prepare('INSERT INTO doctors (id, specialization, created_at, updated_at) VALUES (:id, :specialization, NOW(), NOW())');
              $stmt->execute([
                  ':id' => $uid,
                  ':specialization' => $body['specialization'] ?? null
              ]);
           }

           // 3. Commit changes if all good
           $db->commit();

           Response::json(['success'=>true,'data'=>[
              'id'=>$uid,
              'email'=>$email,
              'role'=>$userRole,
              'name'=>$name,
           ]], 201);

       } catch (\Throwable $e) {
           // 4. Rollback if ANYTHING fails
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

    // Assign patient to doctor
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

       // Verify patient exists
       $stmt = $db->prepare('SELECT id FROM users WHERE id = :id AND role = "PATIENT"');
       $stmt->execute([':id'=>$patientId]);
       if (!$stmt->fetch()) {
          Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Patient not found']], 404);
          return;
       }

       // Verify doctor exists if provided
       if ($doctorId !== null) {
          $stmt = $db->prepare('SELECT id FROM users WHERE id = :id AND role = "DOCTOR"');
          $stmt->execute([':id'=>$doctorId]);
          if (!$stmt->fetch()) {
             Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Doctor not found']], 404);
             return;
          }
       }

       // Update patient assignment
       $stmt = $db->prepare('UPDATE patients SET assigned_doctor_id = :doctor_id, updated_at = NOW() WHERE id = :patient_id');
       $stmt->execute([':doctor_id'=>$doctorId, ':patient_id'=>$patientId]);

       Response::json(['success'=>true,'message'=>'Patient assigned successfully']);
    }

    // Get all doctors (for assignment dropdown)
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