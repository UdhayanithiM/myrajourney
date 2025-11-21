<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\AppointmentModel;
use Src\Utils\Response;
use Src\Models\NotificationModel;

class AppointmentController
{
	private AppointmentModel $appts;
	public function __construct(){ $this->appts = new AppointmentModel(); }

	public function list(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$role = $auth['role'] ?? '';
		
		$page = max(1, (int)($_GET['page'] ?? 1));
		$limit = max(1, min(100, (int)($_GET['limit'] ?? 20)));
		$filters = [];
		
		// Auto-filter based on user role
		if ($role === 'PATIENT') {
			$filters['patient_id'] = $uid;
		} elseif ($role === 'DOCTOR') {
			$filters['doctor_id'] = $uid;
		} else {
			// Admin or explicit filters from query params
			$filters = [
				'patient_id' => $_GET['patient_id'] ?? null,
				'doctor_id' => $_GET['doctor_id'] ?? null,
			];
		}
		
		$r = $this->appts->list($filters, $page, $limit);
		Response::json(['success'=>true,'data'=>$r['items'],'meta'=>['total'=>$r['total'],'page'=>$page,'limit'=>$limit]]);
	}

	public function create(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$role = $auth['role'] ?? '';
		
		$body = json_decode(file_get_contents('php://input'), true) ?? [];
		
		// Auto-set patient_id for PATIENT role
		if ($role === 'PATIENT') {
			$body['patient_id'] = $uid;
		}
		
		$required = ['patient_id','doctor_id','title','start_time','end_time'];
		foreach ($required as $k) { if (empty($body[$k])) { Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>"Missing $k"]],422); return; } }
		$id = $this->appts->create($body);
		$item = $this->appts->find($id);
		// Notify the other party about the appointment
		try {
			$notif = new NotificationModel();
			if ($role === 'DOCTOR') {
				$notif->create((int)$body['patient_id'], 'APPOINTMENT', 'New appointment scheduled', $body['title'] ?? '');
			} else {
				$notif->create((int)$body['doctor_id'], 'APPOINTMENT', 'New appointment request', $body['title'] ?? '');
			}
		} catch (\Throwable $e) { /* ignore */ }
		Response::json(['success'=>true,'data'=>$item], 201);
	}

	public function get(int $id): void
	{
		$item = $this->appts->find($id);
		if (!$item) { Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Not found']],404); return; }
		Response::json(['success'=>true,'data'=>$item]);
	}
}






