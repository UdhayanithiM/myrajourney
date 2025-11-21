<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class ReportModel
{
	private PDO $db;
	public function __construct() { $this->db = DB::conn(); }

	public function list(array $filters, int $page=1, int $limit=20): array
	{
		$w=[];$p=[];
		if (!empty($filters['patient_id'])) { $w[]='patient_id = :pid'; $p[':pid']=(int)$filters['patient_id']; }
		if (!empty($filters['doctor_id'])) { $w[]='doctor_id = :did'; $p[':did']=(int)$filters['doctor_id']; }
		$where = $w ? ('WHERE '.implode(' AND ',$w)) : '';
		$offset = ($page-1)*$limit;
		$total = (int)$this->db->query("SELECT COUNT(*) FROM reports $where")->fetchColumn();
		$stmt=$this->db->prepare("SELECT * FROM reports $where ORDER BY uploaded_at DESC LIMIT :lim OFFSET :off");
		foreach ($p as $k=>$v) $stmt->bindValue($k,$v,PDO::PARAM_INT);
		$stmt->bindValue(':lim',$limit,PDO::PARAM_INT);
		$stmt->bindValue(':off',$offset,PDO::PARAM_INT);
		$stmt->execute();
		return ['items'=>$stmt->fetchAll(),'total'=>$total];
	}

	public function create(array $d): int
	{
		$stmt=$this->db->prepare('INSERT INTO reports (patient_id, doctor_id, title, description, file_url, mime_type, size_bytes, uploaded_at) VALUES (:patient_id,:doctor_id,:title,:description,:file_url,:mime_type,:size_bytes,NOW())');
		$stmt->execute([
			':patient_id'=>(int)$d['patient_id'],
			':doctor_id'=>isset($d['doctor_id']) ? (int)$d['doctor_id'] : null,
			':title'=>$d['title'],
			':description'=>$d['description'] ?? null,
			':file_url'=>$d['file_url'],
			':mime_type'=>$d['mime_type'],
			':size_bytes'=>(int)$d['size_bytes'],
		]);
		return (int)$this->db->lastInsertId();
	}

	public function find(int $id): ?array
	{
		$stmt=$this->db->prepare('SELECT * FROM reports WHERE id=:id');
		$stmt->execute([':id'=>$id]);
		$row=$stmt->fetch();
		return $row ?: null;
	}
}




















