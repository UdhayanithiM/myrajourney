<?php
declare(strict_types=1);

namespace Src\Config;

use PDO;
use PDOException;

class DB
{
	private static ?PDO $pdo = null;

	public static function conn(): PDO
	{
		if (self::$pdo) return self::$pdo;
		$dsn = sprintf('mysql:host=%s;dbname=%s;charset=utf8mb4',
			Config::get('DB_HOST', '127.0.0.1'),
			Config::get('DB_NAME', 'myrajourney')
		);
		try {
			self::$pdo = new PDO($dsn, Config::get('DB_USER', 'root'), Config::get('DB_PASS', ''), [
				PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
				PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
			]);
			return self::$pdo;
		} catch (PDOException $e) {
			die('DB connection failed');
		}
	}
}




















