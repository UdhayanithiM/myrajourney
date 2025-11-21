<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class MedicationModel
{
	private PDO $db;
	public function __construct(){ $this->db = DB::conn(); }

	public function patientMedications(int $patientId, ?int $active, int $page, int $limit): array
	{
		$where = 'WHERE patient_id = :pid';
		$params = [':pid'=>$patientId];
		if ($active !== null) { $where .= ' AND active = :active'; $params[':active']=$active; }
		$offset = ($page-1)*$limit;
		$total = (int)$this->db->prepare("SELECT COUNT(*) FROM patient_medications $where")->execute($params) ?: 0;
		$stmt = $this->db->prepare("SELECT * FROM patient_medications $where ORDER BY created_at DESC LIMIT :lim OFFSET :off");
		foreach ($params as $k=>$v) $stmt->bindValue($k,$v, is_int($v)?PDO::PARAM_INT:PDO::PARAM_STR);
		$stmt->bindValue(':lim',$limit,PDO::PARAM_INT);
		$stmt->bindValue(':off',$offset,PDO::PARAM_INT);
		$stmt->execute();
		return ['items'=>$stmt->fetchAll(),'total'=>$total];
	}

	public function assign(array $d): int
	{
		$stmt=$this->db->prepare('INSERT INTO patient_medications (patient_id, medication_id, name_override, dosage, frequency_per_day, start_date, end_date, active, created_at, updated_at) VALUES (:patient_id,:medication_id,:name_override,:dosage,:frequency_per_day,:start_date,:end_date,1,NOW(),NOW())');
		$stmt->execute([
			':patient_id'=>(int)$d['patient_id'],
			':medication_id'=>$d['medication_id'] ?? null,
			':name_override'=>$d['name_override'] ?? null,
			':dosage'=>$d['dosage'] ?? null,
			':frequency_per_day'=>$d['frequency_per_day'] ?? null,
			':start_date'=>$d['start_date'] ?? null,
			':end_date'=>$d['end_date'] ?? null,
		]);
		return (int)$this->db->lastInsertId();
	}

	public function updateActive(int $id, int $active): void
	{
		$this->db->prepare('UPDATE patient_medications SET active=:a, updated_at=NOW() WHERE id=:id')->execute([':a'=>$active,':id'=>$id]);
	}

	public function logIntake(array $d): int
	{
		$stmt=$this->db->prepare('INSERT INTO medication_logs (patient_medication_id, taken_at, status, created_at) VALUES (:pmid, :taken_at, :status, NOW())');
		$stmt->execute([
			':pmid'=>(int)$d['patient_medication_id'],
			':taken_at'=>$d['taken_at'],
			':status'=>$d['status'],
		]);
		return (int)$this->db->lastInsertId();
	}
}




















