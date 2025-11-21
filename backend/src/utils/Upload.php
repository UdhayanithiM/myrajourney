<?php
declare(strict_types=1);

namespace Src\Utils;

class Upload
{
	public static function saveReport(array $file): array
	{
		$allowed = ['application/pdf','image/png','image/jpeg','image/webp'];
		if (!in_array($file['type'] ?? '', $allowed) || ($file['error'] ?? UPLOAD_ERR_NO_FILE) !== UPLOAD_ERR_OK) {
			throw new \Exception('Invalid file');
		}
		$base = __DIR__ . '/../../storage/uploads/reports/' . date('Y/m');
		if (!is_dir($base)) mkdir($base, 0775, true);
		$ext = pathinfo($file['name'] ?? 'file', PATHINFO_EXTENSION) ?: 'bin';
		$name = bin2hex(random_bytes(8)) . '.' . $ext;
		$path = $base . '/' . $name;
		if (!move_uploaded_file($file['tmp_name'], $path)) throw new \Exception('Move failed');
		$publicPath = '/uploads/reports/' . date('Y/m') . '/' . $name;
		return ['file_url'=>$publicPath,'mime_type'=>$file['type'],'size_bytes'=> (int)$file['size']];
	}
}




















