<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\RehabModel;
use Src\Models\NotificationModel;
use Src\Utils\Response;

class RehabController
{
	private RehabModel $rehab;
	public function __construct(){ $this->rehab = new RehabModel(); }

	public function listForPatient(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$role = $auth['role'] ?? '';
		
		// Auto-set patient_id for PATIENT role
		if ($role === 'PATIENT') {
			$pid = $uid;
		} else {
			// For doctors/admins, require patient_id parameter
			$pid = (int)($_GET['patient_id'] ?? 0);
		}
		
		if (!$pid) {
			Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'patient_id required']],422);
			return;
		}
		
		$data=$this->rehab->plans($pid);
		Response::json(['success'=>true,'data'=>$data]);
	}

	public function createPlan(): void
	{
		$body=json_decode(file_get_contents('php://input'), true) ?? [];
		if(empty($body['patient_id']) || empty($body['title'])){ Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Missing fields']],422); return; }
		$planId=$this->rehab->createPlan($body);
		if (!empty($body['exercises']) && is_array($body['exercises'])) {
			$this->rehab->addExercises($planId, $body['exercises']);
		}
		try { (new NotificationModel())->create((int)$body['patient_id'], 'REHAB', 'Rehab plan updated', (string)$body['title']); } catch (\Throwable $e) { }
		Response::json(['success'=>true,'data'=>$this->rehab->planWithExercises($planId)],201);
	}

	public function getPlan(int $id): void
	{
		$item=$this->rehab->planWithExercises($id);
		if(!$item){ Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Not found']],404); return; }
		Response::json(['success'=>true,'data'=>$item]);
	}
}






