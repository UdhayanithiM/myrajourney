<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\ReportModel;
use Src\Models\NotificationModel;
use Src\Utils\Response;
use Src\Utils\Upload;

class ReportController
{
	private ReportModel $reports;
	public function __construct(){ $this->reports = new ReportModel(); }

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
			$r = $this->reports->list($filters, $page, $limit);
		} elseif ($role === 'DOCTOR') {
			// For doctors, show all reports from patients that have appointments with this doctor
			$db = \Src\Config\DB::conn();
			$stmt = $db->prepare("SELECT DISTINCT patient_id FROM appointments WHERE doctor_id = :did");
			$stmt->execute([':did' => $uid]);
			$patientIds = $stmt->fetchAll(\PDO::FETCH_COLUMN);
			
			if (empty($patientIds)) {
				Response::json(['success'=>true,'data'=>[],'meta'=>['total'=>0,'page'=>$page,'limit'=>$limit]]);
				return;
			}
			
			// Get reports for all patients that have appointments with this doctor
			$placeholders = implode(',', array_fill(0, count($patientIds), '?'));
			$offset = ($page - 1) * $limit;
			
			$countStmt = $db->prepare("SELECT COUNT(*) FROM reports WHERE patient_id IN ($placeholders)");
			$countStmt->execute($patientIds);
			$total = (int)$countStmt->fetchColumn();
			
			$stmt = $db->prepare("SELECT r.*, u.name as patient_name FROM reports r 
				LEFT JOIN users u ON r.patient_id = u.id 
				WHERE r.patient_id IN ($placeholders) 
				ORDER BY r.uploaded_at DESC LIMIT ? OFFSET ?");
			$params = array_merge($patientIds, [$limit, $offset]);
			$stmt->execute($params);
			$items = $stmt->fetchAll();
			
			Response::json(['success'=>true,'data'=>$items,'meta'=>['total'=>$total,'page'=>$page,'limit'=>$limit]]);
			return;
		} else {
			// Admin or explicit filters from query params
			$filters = [
				'patient_id' => $_GET['patient_id'] ?? null,
				'doctor_id' => $_GET['doctor_id'] ?? null,
			];
		}
		
		$r = $this->reports->list($filters, $page, $limit);
		Response::json(['success'=>true,'data'=>$r['items'],'meta'=>['total'=>$r['total'],'page'=>$page,'limit'=>$limit]]);
	}

	public function create(): void
	{
		$patientId = (int)($_POST['patient_id'] ?? 0);
		$title = (string)($_POST['title'] ?? '');
		$description = (string)($_POST['description'] ?? '');
		if (!$patientId || !$title || empty($_FILES['file'])) { Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Missing fields']],422); return; }
        try {
			$fileInfo = Upload::saveReport($_FILES['file']);
			$id = $this->reports->create([
				'patient_id'=>$patientId,
                'title'=>$title,
                'description'=>$description,
                'file_url'=>$fileInfo['file_url'],
                'mime_type'=>$fileInfo['mime_type'],
                'size_bytes'=>$fileInfo['size_bytes'],
			]);
			$item = $this->reports->find($id);
            // Notify linked doctors about new report
            try {
                $notif = new NotificationModel();
                $db = \Src\Config\DB::conn();
                $stmt = $db->prepare('SELECT DISTINCT doctor_id FROM appointments WHERE patient_id = :pid');
                $stmt->execute([':pid'=>$patientId]);
                $doctorIds = $stmt->fetchAll(\PDO::FETCH_COLUMN);
                foreach ($doctorIds as $did) { $notif->create((int)$did, 'PATIENT_REPORT', 'New report uploaded', $title); }
            } catch (\Throwable $e) { /* ignore */ }
			Response::json(['success'=>true,'data'=>$item], 201);
		} catch (\Throwable $e) {
			Response::json(['success'=>false,'error'=>['code'=>'UPLOAD_FAILED','message'=>'Upload failed']], 400);
		}
	}

	public function get(int $id): void
	{
		$item = $this->reports->find($id);
		if (!$item) { Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Not found']],404); return; }
		Response::json(['success'=>true,'data'=>$item]);
	}
}






