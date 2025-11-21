<?php
declare(strict_types=1);

namespace Src\Utils;

class Env
{
	public static function load(string $path): void
	{
		if (!file_exists($path)) return;
		$lines = file($path, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
		foreach ($lines as $line) {
			if (str_starts_with(trim($line), '#')) continue;
			[$key, $value] = array_map('trim', explode('=', $line, 2));
			$_ENV[$key] = trim($value, "\"' ");
		}
	}
}




















