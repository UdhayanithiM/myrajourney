<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\SymptomModel;
use Src\Utils\Response;
use Src\Models\NotificationModel;

class SymptomController
{
	private SymptomModel $sym;
	public function __construct(){ $this->sym = new SymptomModel(); }

	public function list(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$role = $auth['role'] ?? '';
		
		// Determine patient_id based on role
		if ($role === 'PATIENT') {
			$pid = $uid; // Patient sees their own symptoms
		} elseif ($role === 'DOCTOR') {
			// Doctor can see any patient's symptoms if patient_id is provided
			$pid = (int)($_GET['patient_id'] ?? 0);
			if (!$pid) {
				// If no patient_id, get all patients from doctor's appointments
				$db = \Src\Config\DB::conn();
				$stmt = $db->prepare("SELECT DISTINCT patient_id FROM appointments WHERE doctor_id = :did");
				$stmt->execute([':did' => $uid]);
				$patientIds = $stmt->fetchAll(\PDO::FETCH_COLUMN);
				if (empty($patientIds)) {
					Response::json(['success'=>true,'data'=>[]]);
					return;
				}
				// Return first patient's symptoms (can be enhanced to return all)
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
		
		$from=$_GET['from'] ?? null;
		$to=$_GET['to'] ?? null;
		$data=$this->sym->list($pid,$from,$to);
		Response::json(['success'=>true,'data'=>$data]);
	}

	public function create(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$role = $auth['role'] ?? '';
		
		$body=json_decode(file_get_contents('php://input'), true) ?? [];
		
		// Auto-set patient_id for PATIENT role
		if ($role === 'PATIENT') {
			$body['patient_id'] = $uid;
		}
		
		foreach(['patient_id','date','pain_level','stiffness_level','fatigue_level'] as $k){ if(empty($body[$k])){ Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>"Missing $k"]],422); return; } }
		$id=$this->sym->create($body);
		// Notify the patient's doctors
		try {
			$notif = new NotificationModel();
			$db = \Src\Config\DB::conn();
			$stmt = $db->prepare('SELECT DISTINCT doctor_id FROM appointments WHERE patient_id = :pid');
			$stmt->execute([':pid'=>(int)$body['patient_id']]);
			$doctorIds = $stmt->fetchAll(\PDO::FETCH_COLUMN);
			foreach ($doctorIds as $did) {
				$notif->create((int)$did, 'PATIENT_SYMPTOM', 'New symptom log from patient', 'Patient updated symptoms.');
			}
		} catch (\Throwable $e) { /* ignore */ }
		Response::json(['success'=>true,'data'=>['id'=>$id]],201);
	}
}






