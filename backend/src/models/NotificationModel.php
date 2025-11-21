<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class NotificationModel
{
	private PDO $db;
	public function __construct(){ $this->db = DB::conn(); }

    public function create(int $userId, string $type, string $title, ?string $body = null): int
    {
        $stmt = $this->db->prepare('INSERT INTO notifications (user_id, type, title, body, created_at) VALUES (:uid,:type,:title,:body,NOW())');
        $stmt->execute([
            ':uid'=>$userId,
            ':type'=>$type,
            ':title'=>$title,
            ':body'=>$body,
        ]);
        return (int)$this->db->lastInsertId();
    }

	public function list(int $userId, int $page, int $limit, ?bool $unread): array
	{
		$where = 'WHERE user_id = :uid';
		$params = [':uid'=>$userId];
		if ($unread !== null) { $where .= ' AND read_at '.($unread?'IS NULL':'IS NOT NULL'); }
		$offset = ($page-1)*$limit;
		$total = (int)$this->db->prepare("SELECT COUNT(*) FROM notifications $where")->execute($params) ?: 0;
		$stmt = $this->db->prepare("SELECT * FROM notifications $where ORDER BY created_at DESC LIMIT :lim OFFSET :off");
		$stmt->bindValue(':lim',$limit,PDO::PARAM_INT);
		$stmt->bindValue(':off',$offset,PDO::PARAM_INT);
		$stmt->execute($params);
		return ['items'=>$stmt->fetchAll(),'total'=>$total];
	}

	public function markRead(int $id, int $userId): void
	{
		$this->db->prepare('UPDATE notifications SET read_at = NOW() WHERE id = :id AND user_id = :uid')->execute([':id'=>$id,':uid'=>$userId]);
	}
}






