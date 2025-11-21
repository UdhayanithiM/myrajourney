<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Utils\Response;
use Src\Config\DB;

class PatientController
{
	public function overviewMe(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$db = DB::conn();
		$nextAppt = $db->prepare("SELECT * FROM appointments WHERE patient_id = :uid AND start_time >= NOW() ORDER BY start_time ASC LIMIT 1");
		$nextAppt->execute([':uid'=>$uid]);
		$latestReports = $db->prepare("SELECT id,title,uploaded_at FROM reports WHERE patient_id = :uid ORDER BY uploaded_at DESC LIMIT 5");
		$latestReports->execute([':uid'=>$uid]);
		$unread = $db->prepare("SELECT COUNT(*) FROM notifications WHERE user_id = :uid AND read_at IS NULL");
		$unread->execute([':uid'=>$uid]);
		$stats = $db->prepare("SELECT metric_type, metric_value, unit, recorded_at FROM health_metrics WHERE patient_id=:uid ORDER BY recorded_at DESC LIMIT 5");
		$stats->execute([':uid'=>$uid]);
		Response::json(['success'=>true,'data'=>[
			'upcomingAppointment'=>$nextAppt->fetch() ?: null,
			'recentReports'=>$latestReports->fetchAll(),
			'unreadNotifications'=>(int)$unread->fetchColumn(),
			'latestMetrics'=>$stats->fetchAll(),
		]]);
	}

	// Get all patients (for doctors/admins)
	public function listAll(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$role = $auth['role'] ?? '';
		
		$db = DB::conn();
		
		if ($role === 'DOCTOR') {
			error_log("Entering DOCTOR branch");
			// Doctors can see only their assigned patients
			$stmt = $db->prepare("SELECT u.id, u.name, u.email, u.phone, u.role, u.created_at, 
				p.dob, p.gender, p.medical_id, p.address, p.assigned_doctor_id
				FROM users u
				LEFT JOIN patients p ON u.id = p.id
				WHERE u.role = 'PATIENT' AND u.status = 'ACTIVE' AND p.assigned_doctor_id = :doctor_id
				ORDER BY u.created_at DESC");
			$stmt->execute([':doctor_id'=>$uid]);
			$patients = $stmt->fetchAll();
			error_log("Doctor $uid - Found " . count($patients) . " assigned patients");
			Response::json(['success'=>true,'data'=>$patients,'debug'=>['doctor_id'=>$uid,'count'=>count($patients),'role'=>$role]]);
		} elseif ($role === 'ADMIN') {
			error_log("Entering ADMIN branch");
			// Admins can see ALL users (patients and doctors) for assignment purposes
			$stmt = $db->prepare("SELECT u.id, u.name, u.email, u.phone, u.role, u.created_at, 
				p.dob, p.gender, p.medical_id, p.address, p.assigned_doctor_id,
				d.name as doctor_name, doc.specialization
				FROM users u
				LEFT JOIN patients p ON u.id = p.id
				LEFT JOIN users d ON p.assigned_doctor_id = d.id
				LEFT JOIN doctors doc ON u.id = doc.id
				WHERE u.role IN ('PATIENT', 'DOCTOR') AND u.status = 'ACTIVE'
				ORDER BY u.created_at DESC");
			$stmt->execute();
			$patients = $stmt->fetchAll();
			error_log("Admin - Found " . count($patients) . " users");
			Response::json(['success'=>true,'data'=>$patients,'debug'=>['role'=>'ADMIN','count'=>count($patients)]]);
		} else {
			error_log("No valid role - Access denied. Role: $role");
			Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Access denied'],'debug'=>['role'=>$role,'uid'=>$uid]], 403);
		}
	}
}
