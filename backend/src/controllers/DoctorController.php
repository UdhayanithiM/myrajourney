<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Config\DB;
use Src\Utils\Response;

class DoctorController
{
	public function overview(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$db = DB::conn();
		$today = date('Y-m-d');
		$schedule = $db->prepare("SELECT * FROM appointments WHERE doctor_id=:uid AND DATE(start_time)=:d ORDER BY start_time ASC");
		$schedule->execute([':uid'=>$uid, ':d'=>$today]);
		
		// Count only assigned patients' reports
		$reportCount = (int)$db->prepare("SELECT COUNT(*) FROM reports r 
			INNER JOIN patients p ON r.patient_id = p.id 
			WHERE p.assigned_doctor_id = :uid AND r.uploaded_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
			->execute([':uid'=>$uid]) ? $db->query("SELECT FOUND_ROWS()")->fetchColumn() : 0;
		
		// Count assigned patients
		$patientCount = (int)$db->prepare("SELECT COUNT(*) FROM patients WHERE assigned_doctor_id = :uid")
			->execute([':uid'=>$uid]) ? $db->query("SELECT FOUND_ROWS()")->fetchColumn() : 0;
		
		Response::json(['success'=>true,'data'=>[
			'todaySchedule'=>$schedule->fetchAll(),
			'recentReportsCount'=>$reportCount,
			'patientsCount'=>$patientCount,
		]]);
	}
}




















