<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Utils\Response;
use Src\Middlewares\Auth;
use Src\Config\DB;

class ReportNoteController
{
    public function create(): void
    {
        Auth::requireAuth();
        $auth = $_SERVER['auth'] ?? [];
        $doctorId = (int)($auth['uid'] ?? 0);
        $role = $auth['role'] ?? '';
        
        // Only doctors can add notes
        if ($role !== 'DOCTOR') {
            Response::json(['success'=>false,'error'=>['code'=>'FORBIDDEN','message'=>'Only doctors can add report notes']], 403);
            return;
        }
        
        $body = json_decode(file_get_contents('php://input'), true) ?? [];
        $reportId = (int)($body['report_id'] ?? 0);
        $diagnosis = (string)($body['diagnosis_text'] ?? '');
        $suggestions = (string)($body['suggestions_text'] ?? '');
        
        if (!$reportId) {
            Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'report_id required']],422);
            return;
        }
        
        if (empty($diagnosis) && empty($suggestions)) {
            Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'diagnosis_text or suggestions_text required']],422);
            return;
        }
        
        $db = DB::conn();
        
        // Check if report exists
        $stmt = $db->prepare('SELECT id FROM reports WHERE id = :id');
        $stmt->execute([':id' => $reportId]);
        if (!$stmt->fetch()) {
            Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'Report not found']],404);
            return;
        }
        
        // Check if note already exists for this report and doctor
        $stmt = $db->prepare('SELECT id FROM report_notes WHERE report_id = :rid AND doctor_id = :did');
        $stmt->execute([':rid' => $reportId, ':did' => $doctorId]);
        $existing = $stmt->fetch();
        
        if ($existing) {
            // Update existing note
            $stmt = $db->prepare('UPDATE report_notes SET diagnosis_text = :diag, suggestions_text = :sugg, updated_at = NOW() WHERE id = :id');
            $stmt->execute([
                ':diag' => $diagnosis ?: null,
                ':sugg' => $suggestions ?: null,
                ':id' => (int)$existing['id']
            ]);
            $noteId = (int)$existing['id'];
        } else {
            // Create new note
            $stmt = $db->prepare('INSERT INTO report_notes (report_id, doctor_id, diagnosis_text, suggestions_text, created_at, updated_at) VALUES (:rid, :did, :diag, :sugg, NOW(), NOW())');
            $stmt->execute([
                ':rid' => $reportId,
                ':did' => $doctorId,
                ':diag' => $diagnosis ?: null,
                ':sugg' => $suggestions ?: null
            ]);
            $noteId = (int)$db->lastInsertId();
        }
        
        // Get the created/updated note
        $stmt = $db->prepare('SELECT * FROM report_notes WHERE id = :id');
        $stmt->execute([':id' => $noteId]);
        $note = $stmt->fetch();
        
        // Notify patient
        try {
            $stmt = $db->prepare('SELECT patient_id FROM reports WHERE id = :id');
            $stmt->execute([':id' => $reportId]);
            $report = $stmt->fetch();
            if ($report) {
                $notif = new \Src\Models\NotificationModel();
                $notif->create((int)$report['patient_id'], 'REPORT_NOTE', 'Doctor added diagnosis', 'Your report has been reviewed.');
            }
        } catch (\Throwable $e) { /* ignore */ }
        
        Response::json(['success'=>true,'data'=>$note], 201);
    }
    
    public function get(int $reportId): void
    {
        Auth::requireAuth();
        $db = DB::conn();
        
        $stmt = $db->prepare('SELECT rn.*, u.name as doctor_name FROM report_notes rn LEFT JOIN users u ON rn.doctor_id = u.id WHERE rn.report_id = :rid ORDER BY rn.created_at DESC');
        $stmt->execute([':rid' => $reportId]);
        $notes = $stmt->fetchAll();
        
        Response::json(['success'=>true,'data'=>$notes]);
    }
}

