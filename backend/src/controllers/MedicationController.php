<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\MedicationModel;
use Src\Utils\Response;
use Src\Models\NotificationModel;

class MedicationController
{
	private MedicationModel $meds;
	public function __construct(){ $this->meds = new MedicationModel(); }

	public function listForPatient(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$role = $auth['role'] ?? '';
		
		// Determine patient_id based on role
		if ($role === 'PATIENT') {
			$pid = $uid; // Patient sees their own medications
		} elseif ($role === 'DOCTOR') {
			// Doctor can see any patient's medications if patient_id is provided
			$pid = (int)($_GET['patient_id'] ?? 0);
			if (!$pid) {
				// If no patient_id, get all patients from doctor's appointments
				$db = \Src\Config\DB::conn();
				$stmt = $db->prepare("SELECT DISTINCT patient_id FROM appointments WHERE doctor_id = :did");
				$stmt->execute([':did' => $uid]);
				$patientIds = $stmt->fetchAll(\PDO::FETCH_COLUMN);
				if (empty($patientIds)) {
					Response::json(['success'=>true,'data'=>[],'meta'=>['total'=>0,'page'=>1,'limit'=>20]]);
					return;
				}
				// For now, return first patient's medications (can be enhanced to return all)
				$pid = (int)($patientIds[0] ?? 0);
			}
		} else {
			// Admin or explicit patient_id
			$pid = (int)($_GET['patient_id'] ?? 0);
		}
		
		if (!$pid) {
			Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'patient_id required']],422);
			return;
		}
		
		$active = isset($_GET['active']) ? (int)($_GET['active'] === '1') : null;
		$page = max(1, (int)($_GET['page'] ?? 1));
		$limit = max(1, min(100, (int)($_GET['limit'] ?? 20)));
		$r = $this->meds->patientMedications($pid, $active, $page, $limit);
		Response::json(['success'=>true,'data'=>$r['items'],'meta'=>['total'=>$r['total'],'page'=>$page,'limit'=>$limit]]);
	}

	public function assign(): void
	{
		$body = json_decode(file_get_contents('php://input'), true) ?? [];
		$required=['patient_id'];
		foreach($required as $k){ if(empty($body[$k])){ Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>"Missing $k"]],422); return; } }
		$id = $this->meds->assign($body);
		// Notify patient about new/updated medication assignment
		try {
			$notif = new NotificationModel();
			$notif->create((int)$body['patient_id'], 'DOCTOR_MEDICATION', 'Medication updated', 'Your medication plan has been updated.');
		} catch (\Throwable $e) { /* ignore */ }
		Response::json(['success'=>true,'data'=>['id'=>$id]],201);
	}

	public function setActive(int $id): void
	{
		$body = json_decode(file_get_contents('php://input'), true) ?? [];
		$active = (int) (!empty($body['active']));
		$this->meds->updateActive($id, $active);
		Response::json(['success'=>true]);
	}

	public function logIntake(): void
	{
		$body = json_decode(file_get_contents('php://input'), true) ?? [];
		$required=['patient_medication_id','status','taken_at'];
		foreach($required as $k){ if(empty($body[$k])){ Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>"Missing $k"]],422); return; } }
		$id = $this->meds->logIntake($body);
		// Notify doctor(s) that patient logged medication
		try {
			$notif = new NotificationModel();
			$db = \Src\Config\DB::conn();
			// Find patient_id from patient_medications
			$pm = $db->prepare('SELECT patient_id FROM patient_medications WHERE id = :pmid');
			$pm->execute([':pmid'=>(int)$body['patient_medication_id']]);
			$pid = (int)$pm->fetchColumn();
			if ($pid) {
				$stmt = $db->prepare('SELECT DISTINCT doctor_id FROM appointments WHERE patient_id = :pid');
				$stmt->execute([':pid'=>$pid]);
				$doctorIds = $stmt->fetchAll(\PDO::FETCH_COLUMN);
				foreach ($doctorIds as $did) { $notif->create((int)$did, 'PATIENT_MEDICATION_LOG', 'Medication intake logged', null); }
			}
		} catch (\Throwable $e) { /* ignore */ }
		Response::json(['success'=>true,'data'=>['id'=>$id]],201);
	}
}






