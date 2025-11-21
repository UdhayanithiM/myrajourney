<?php
// Test database connection with detailed error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "<h2>Database Connection Debug</h2>";

// Load .env file
$envFile = __DIR__ . '/backend/.env';
if (!file_exists($envFile)) {
    $envFile = __DIR__ . '/../.env';
}

if (file_exists($envFile)) {
    $lines = file($envFile, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        if (strpos(trim($line), '#') === 0) continue;
        list($key, $value) = explode('=', $line, 2);
        $_ENV[trim($key)] = trim($value);
    }
    echo "<p>✅ .env file loaded from: $envFile</p>";
} else {
    echo "<p>❌ .env file not found</p>";
}

$host = $_ENV['DB_HOST'] ?? '127.0.0.1';
$port = $_ENV['DB_PORT'] ?? '3306';
$dbname = $_ENV['DB_NAME'] ?? 'myrajourney';
$user = $_ENV['DB_USER'] ?? 'root';
$pass = $_ENV['DB_PASS'] ?? '';

echo "<h3>Connection Parameters:</h3>";
echo "<ul>";
echo "<li>Host: $host</li>";
echo "<li>Port: $port</li>";
echo "<li>Database: $dbname</li>";
echo "<li>User: $user</li>";
echo "<li>Password: " . (empty($pass) ? '(empty)' : '(set)') . "</li>";
echo "</ul>";

// Try to connect
try {
    $dsn = "mysql:host=$host;port=$port;charset=utf8mb4";
    $pdo = new PDO($dsn, $user, $pass, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    ]);
    echo "<p>✅ Connected to MySQL server</p>";
    
    // Check if database exists
    $stmt = $pdo->query("SHOW DATABASES LIKE '$dbname'");
    $exists = $stmt->fetch();
    
    if ($exists) {
        echo "<p>✅ Database '$dbname' exists</p>";
        
        // Connect to database
        $pdo->exec("USE $dbname");
        
        // Show tables
        $stmt = $pdo->query("SHOW TABLES");
        $tables = $stmt->fetchAll(PDO::FETCH_COLUMN);
        
        echo "<h3>Tables in database:</h3>";
        echo "<ul>";
        foreach ($tables as $table) {
            echo "<li>$table</li>";
        }
        echo "</ul>";
        
        // Count users
        $stmt = $pdo->query("SELECT COUNT(*) as count FROM users");
        $count = $stmt->fetch();
        echo "<p>Total users: " . $count['count'] . "</p>";
        
    } else {
        echo "<p>❌ Database '$dbname' does not exist</p>";
        echo "<p>Available databases:</p>";
        $stmt = $pdo->query("SHOW DATABASES");
        $databases = $stmt->fetchAll(PDO::FETCH_COLUMN);
        echo "<ul>";
        foreach ($databases as $db) {
            echo "<li>$db</li>";
        }
        echo "</ul>";
    }
    
} catch (PDOException $e) {
    echo "<p>❌ Connection failed: " . $e->getMessage() . "</p>";
    echo "<p>Error Code: " . $e->getCode() . "</p>";
}
