<?php
// Try different password combinations
$passwords = ['', 'root', 'password', 'admin', '123456', 'mysql'];
$host = '127.0.0.1';
$port = '3306';
$user = 'root';

echo "<h2>Testing MySQL Passwords</h2>";

foreach ($passwords as $pass) {
    try {
        $dsn = "mysql:host=$host;port=$port;charset=utf8mb4";
        $pdo = new PDO($dsn, $user, $pass, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        ]);
        echo "<p>✅ SUCCESS! Password is: '" . ($pass === '' ? '(empty)' : $pass) . "'</p>";
        
        // Check for myrajourney database
        $stmt = $pdo->query("SHOW DATABASES LIKE 'myrajourney'");
        if ($stmt->fetch()) {
            echo "<p>✅ Database 'myrajourney' exists!</p>";
        } else {
            echo "<p>❌ Database 'myrajourney' does not exist. Need to create it.</p>";
        }
        break;
    } catch (PDOException $e) {
        echo "<p>❌ Password '" . ($pass === '' ? '(empty)' : $pass) . "' failed</p>";
    }
}
