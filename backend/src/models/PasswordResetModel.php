<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class PasswordResetModel
{
    private PDO $db;
    public function __construct(){ $this->db = DB::conn(); }

    public function createToken(int $userId, string $token, int $ttlSeconds): void
    {
        $stmt = $this->db->prepare('INSERT INTO password_resets (user_id, token, expires_at, created_at) VALUES (:uid,:tok,DATE_ADD(NOW(), INTERVAL :ttl SECOND), NOW())');
        $stmt->bindValue(':uid', $userId, PDO::PARAM_INT);
        $stmt->bindValue(':tok', $token, PDO::PARAM_STR);
        $stmt->bindValue(':ttl', $ttlSeconds, PDO::PARAM_INT);
        $stmt->execute();
    }

    public function findValid(string $token): ?array
    {
        $stmt = $this->db->prepare('SELECT * FROM password_resets WHERE token = :tok AND expires_at > NOW()');
        $stmt->execute([':tok'=>$token]);
        $row = $stmt->fetch();
        return $row ?: null;
    }

    public function consume(int $id): void
    {
        $this->db->prepare('DELETE FROM password_resets WHERE id = :id')->execute([':id'=>$id]);
    }
}


