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

    private const LOG_ENABLED = true;
    private const LOG_FILE = __DIR__ . '/../../storage/logs/report_debug.log';

    public function __construct()
    {
        $this->reports = new ReportModel();
    }

    private function log(string $msg): void
    {
        if (!self::LOG_ENABLED) return;

        $line = '[' . date('Y-m-d H:i:s') . '] ' . $msg . PHP_EOL;

        $dir = dirname(self::LOG_FILE);
        if (!is_dir($dir)) mkdir($dir, 0775, true);

        file_put_contents(self::LOG_FILE, $line, FILE_APPEND);
    }


    /* ============================================================
       LIST REPORTS
       ============================================================ */
    public function list(): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $uid  = (int)($auth['uid'] ?? 0);
        $role = $auth['role'] ?? '';

        $page  = max(1, (int)($_GET['page'] ?? 1));
        $limit = max(1, min(100, (int)($_GET['limit'] ?? 20)));
        $offset = ($page - 1) * $limit;

        /* PATIENT */
        if ($role === 'PATIENT') {

            $r = $this->reports->list(['patient_id' => $uid], $page, $limit);

            Response::json([
                'success' => true,
                'data'    => $this->fixUrls($r['items']),
                'meta'    => ['total' => $r['total'], 'page' => $page, 'limit' => $limit]
            ]);
            exit;
        }

        /* DOCTOR */
        if ($role === 'DOCTOR') {

            $db = \Src\Config\DB::conn();

            $stmt = $db->prepare("SELECT DISTINCT patient_id FROM appointments WHERE doctor_id = :d");
            $stmt->execute([':d' => $uid]);
            $patientIds = $stmt->fetchAll(\PDO::FETCH_COLUMN);

            if (empty($patientIds)) {
                Response::json([
                    'success' => true,
                    'data' => [],
                    'meta' => ['total' => 0]
                ]);
                exit;
            }

            $ph = implode(',', array_fill(0, count($patientIds), '?'));

            // count
            $countStmt = $db->prepare("SELECT COUNT(*) FROM reports WHERE patient_id IN ($ph)");
            foreach ($patientIds as $i => $pid) {
                $countStmt->bindValue($i + 1, (int)$pid);
            }
            $countStmt->execute();
            $total = (int)$countStmt->fetchColumn();

            // data
            $sql = "SELECT r.*, u.name AS patient_name
                    FROM reports r
                    LEFT JOIN users u ON r.patient_id = u.id
                    WHERE r.patient_id IN ($ph)
                    ORDER BY r.uploaded_at DESC
                    LIMIT ? OFFSET ?";

            $stmt = $db->prepare($sql);

            $pos = 1;
            foreach ($patientIds as $pid) {
                $stmt->bindValue($pos++, (int)$pid);
            }

            $stmt->bindValue($pos++, (int)$limit, \PDO::PARAM_INT);
            $stmt->bindValue($pos, (int)$offset, \PDO::PARAM_INT);

            $stmt->execute();
            $items = $stmt->fetchAll();

            Response::json([
                'success' => true,
                'data'    => $this->fixUrls($items),
                'meta'    => ['total' => $total, 'page' => $page, 'limit' => $limit]
            ]);
            exit;
        }

        /* ADMIN */
        $filters = [
            'patient_id' => $_GET['patient_id'] ?? null,
            'doctor_id'  => $_GET['doctor_id'] ?? null
        ];

        $r = $this->reports->list($filters, $page, $limit);

        Response::json([
            'success' => true,
            'data'    => $this->fixUrls($r['items']),
            'meta'    => ['total' => $r['total'], 'page' => $page, 'limit' => $limit]
        ]);
        exit;
    }


    /* ============================================================
       CREATE
       ============================================================ */
    public function create(): void
    {
        $patientId = (int)($_POST['patient_id'] ?? 0);
        $title     = trim($_POST['title'] ?? '');
        $desc      = trim($_POST['description'] ?? '');

        if (!$patientId || !$title || empty($_FILES['file'])) {
            Response::json(['success' => false, 'message' => 'Missing fields'], 422);
            exit;
        }

        try {
            $fileInfo = Upload::saveReport($_FILES['file']);

            $id = $this->reports->create([
                'patient_id' => $patientId,
                'title'      => $title,
                'description'=> $desc,
                'file_url'   => $fileInfo['file_url'],
                'mime_type'  => $fileInfo['mime_type'],
                'size_bytes' => $fileInfo['size_bytes'],
                'status'     => 'Pending'
            ]);

            $item = $this->reports->find($id);

            Response::json(['success' => true, 'data' => $this->fixUrl($item)], 201);
            exit;

        } catch (\Throwable $e) {
            Response::json(['success' => false, 'message' => 'Upload failed'], 400);
            exit;
        }
    }


    /* ============================================================
       GET ONE
       ============================================================ */
    public function get(int $id): void
    {
        $item = $this->reports->find($id);

        if (!$item) {
            Response::json(['success' => false, 'message' => 'Not found'], 404);
            exit;
        }

        Response::json(['success' => true, 'data' => $this->fixUrl($item)]);
        exit;
    }


    /* ============================================================
       UPDATE STATUS
       ============================================================ */
    public function updateStatus(): void
    {
        $body = json_decode(file_get_contents("php://input"), true);

        if (!isset($body['report_id'], $body['status'])) {
            Response::json(['success' => false, 'message' => 'Missing fields'], 422);
            exit;
        }

        $id = (int)$body['report_id'];
        $status = trim($body['status']);

        $ok = $this->reports->update($id, ['status' => $status]);

        if (!$ok) {
            Response::json(['success' => false, 'message' => 'Update failed'], 500);
            exit;
        }

        Response::json(['success' => true, 'message' => 'Status updated']);
        exit;
    }


    /* ============================================================
       URL FIX
       ============================================================ */
    private function fixUrls(array $items): array
    {
        foreach ($items as &$i) {
            if (!empty($i['file_url'])) {
                $i['file_url'] = $this->fullUrl($i['file_url']);
            }
        }
        return $items;
    }

    private function fixUrl(array $item): array
    {
        if (!empty($item['file_url'])) {
            $item['file_url'] = $this->fullUrl($item['file_url']);
        }
        return $item;
    }

   private function fullUrl(string $path): string
   {
       $scheme = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off')
           ? 'https'
           : 'http';

       $host = $scheme . '://' . ($_SERVER['HTTP_HOST'] ?? 'localhost');

       return $host . $path;
   }

}
