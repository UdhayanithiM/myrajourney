<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class SymptomModel
{
	private PDO $db;
	public function __construct(){ $this->db = DB::conn(); }

	public function list(int $patientId, ?string $from, ?string $to): array
	{
		$where='WHERE patient_id=:pid'; $p=[':pid'=>$patientId];
		if ($from) { $where.=' AND `date` >= :f'; $p[':f']=$from; }
		if ($to) { $where.=' AND `date` <= :t'; $p[':t']=$to; }
		$stmt=$this->db->prepare("SELECT * FROM symptom_logs $where ORDER BY `date` DESC");
		$stmt->execute($p);
		return $stmt->fetchAll();
	}

	public function create(array $d): int
	{
		$stmt=$this->db->prepare('INSERT INTO symptom_logs (patient_id, `date`, pain_level, stiffness_level, fatigue_level, notes, created_at) VALUES (:pid,:date,:pain,:stiff,:fatigue,:notes,NOW())');
		$stmt->execute([
			':pid'=>(int)$d['patient_id'],
			':date'=>$d['date'],
			':pain'=>$d['pain_level'],
			':stiff'=>$d['stiffness_level'],
			':fatigue'=>$d['fatigue_level'],
			':notes'=>$d['notes'] ?? null,
		]);
		return (int)$this->db->lastInsertId();
	}
}




















