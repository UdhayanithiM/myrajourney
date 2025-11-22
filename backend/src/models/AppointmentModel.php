<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class AppointmentModel
{
    private PDO $db;
    public function __construct() { $this->db = DB::conn(); }

    public function list(array $filters, int $page = 1, int $limit = 20): array
    {
       $w = [];
       $p = [];

       // Build the WHERE clause and parameters
       if (!empty($filters['patient_id'])) {
           $w[] = 'a.patient_id = :pid';
           $p[':pid'] = (int)$filters['patient_id'];
       }
       if (!empty($filters['doctor_id'])) {
           $w[] = 'a.doctor_id = :did';
           $p[':did'] = (int)$filters['doctor_id'];
       }

       $where = $w ? ('WHERE ' . implode(' AND ', $w)) : '';

       // --- FIX START: Use Prepare/Execute for Count Query ---
       // The original code crashed here because it used query() with placeholders
       $countSql = "SELECT COUNT(*) FROM appointments a $where";
       $stmtCount = $this->db->prepare($countSql);
       foreach ($p as $k => $v) {
           $stmtCount->bindValue($k, $v, PDO::PARAM_INT);
       }
       $stmtCount->execute();
       $total = (int)$stmtCount->fetchColumn();
       // --- FIX END ---

       // Main Query
       $offset = ($page - 1) * $limit;
       $stmt = $this->db->prepare("SELECT a.*,
          pu.name as patient_name, pu.email as patient_email,
          du.name as doctor_name, du.email as doctor_email
          FROM appointments a
          LEFT JOIN users pu ON a.patient_id = pu.id
          LEFT JOIN users du ON a.doctor_id = du.id
          $where ORDER BY a.start_time DESC LIMIT :lim OFFSET :off");

       // Bind search params
       foreach ($p as $k => $v) {
           $stmt->bindValue($k, $v, PDO::PARAM_INT);
       }
       // Bind pagination params
       $stmt->bindValue(':lim', $limit, PDO::PARAM_INT);
       $stmt->bindValue(':off', $offset, PDO::PARAM_INT);

       $stmt->execute();

       return ['items' => $stmt->fetchAll(), 'total' => $total];
    }

    public function create(array $d): int
    {
       $stmt = $this->db->prepare('INSERT INTO appointments (patient_id, doctor_id, title, notes, location, start_time, end_time, status, created_at, updated_at) VALUES (:patient_id, :doctor_id, :title, :notes, :location, :start_time, :end_time, \'SCHEDULED\', NOW(), NOW())');
       $stmt->execute([
          ':patient_id' => (int)$d['patient_id'],
          ':doctor_id' => (int)$d['doctor_id'],
          ':title' => $d['title'],
          ':notes' => $d['notes'] ?? null,
          ':location' => $d['location'] ?? null,
          ':start_time' => $d['start_time'],
          ':end_time' => $d['end_time'],
       ]);
       return (int)$this->db->lastInsertId();
    }

    public function find(int $id): ?array
    {
       $stmt = $this->db->prepare('SELECT * FROM appointments WHERE id=:id');
       $stmt->execute([':id' => $id]);
       $row = $stmt->fetch();
       return $row ?: null;
    }
}