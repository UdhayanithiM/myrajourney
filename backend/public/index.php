<?php
declare(strict_types=1);

require __DIR__ . '/../src/bootstrap.php';

use Src\Utils\Response;
use Src\Middlewares\Auth;
use Src\Controllers\AuthController;
use Src\Controllers\UserController;
use Src\Controllers\PatientController;
use Src\Controllers\DoctorController;
use Src\Controllers\AppointmentController;
use Src\Controllers\ReportController;
use Src\Controllers\MedicationController;
use Src\Controllers\RehabController;
use Src\Controllers\NotificationController;
use Src\Controllers\EducationController;
use Src\Controllers\SymptomController;
use Src\Controllers\MetricController;
use Src\Controllers\SettingsController;
use Src\Controllers\AdminController;
use Src\Controllers\ReportNoteController;

// Basic CORS handling for preflight
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
	Src\Config\Cors::preflight();
	exit;
}
Src\Config\Cors::allow();

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
$uri = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
// Strip the base path if present (e.g., /backend/public)
// DOCUMENT_ROOT is usually C:\xampp\htdocs, __DIR__ is C:\xampp\htdocs\backend\public
$docRoot = str_replace('\\', '/', rtrim($_SERVER['DOCUMENT_ROOT'] ?? '', '/\\'));
$scriptDir = str_replace('\\', '/', rtrim(__DIR__, '/\\'));
$basePath = str_replace($docRoot, '', $scriptDir);
if ($basePath && strpos($uri, $basePath) === 0) {
	$uri = substr($uri, strlen($basePath));
}
// Ensure URI starts with /
if ($uri === '' || ($uri[0] !== '/')) {
	$uri = '/' . $uri;
}

// Exclude test files and non-API files from routing
// But allow specific utility files
$allowedFiles = ['admin-api.php', 'doctor-patients.php', 'clear-cache.php'];
if (in_array(basename($uri), $allowedFiles)) {
	// Let these files execute directly
	return;
}

// If the request is not for an API endpoint and the file exists, let Apache serve it
$requestedFile = $docRoot . $basePath . $uri;
if (strpos($uri, '/api/v1/') !== 0) {
	// Check if it's a test file or other non-API file
	$testFiles = ['test-', 'debug-', 'api-info.php'];
	$isTestFile = false;
	foreach ($testFiles as $prefix) {
		if (strpos(basename($uri), $prefix) === 0) {
			$isTestFile = true;
			break;
		}
	}
	
	// If it's a test file or the actual file exists, return 404 (let Apache handle it)
	if ($isTestFile || file_exists($requestedFile)) {
		http_response_code(404);
		Response::json([
			'success' => false,
			'error' => [
				'code' => 'NOT_FOUND',
				'message' => 'File not found. If this is a test file, access it directly via browser.'
			]
		], 404);
		exit;
	}
}

// Debug: uncomment to see what URI is being processed
// error_log("Method: $method, URI: $uri");

// Simple router
function route(string $method, string $path): bool {
	global $uri;
	return $_SERVER['REQUEST_METHOD'] === $method && $uri === $path;
}

// Auth
if (route('POST', '/api/v1/auth/register')) {
	(new AuthController())->register();
	exit;
}
if (route('POST', '/api/v1/auth/login')) {
	(new AuthController())->login();
	exit;
}
if (route('GET', '/api/v1/auth/me')) {
	Auth::requireAuth();
	(new AuthController())->me();
	exit;
}
// Forgot/Reset Password
if (route('POST', '/api/v1/auth/forgot-password')) {
    (new AuthController())->forgotPassword();
    exit;
}
if (route('POST', '/api/v1/auth/reset-password')) {
    (new AuthController())->resetPassword();
    exit;
}
if (route('POST', '/api/v1/auth/change-password')) {
    Auth::requireAuth();
    (new AuthController())->changePassword();
    exit;
}

// User self update (settings stub)
if (route('PUT', '/api/v1/users/me')) {
	Auth::requireAuth();
	(new UserController())->updateMe();
	exit;
}

// Patient
if (route('GET', '/api/v1/patients/me/overview')) {
	Auth::requireAuth();
	(new PatientController())->overviewMe();
	exit;
}
if (route('GET', '/api/v1/patients')) {
	Auth::requireAuth();
	(new PatientController())->listAll();
	exit;
}

// Admin
if (route('GET', '/api/v1/admin/test')) {
	Response::json(['success'=>true,'message'=>'Admin routes working','uri'=>$uri]);
	exit;
}
if (route('POST', '/api/v1/admin/users')) {
	Auth::requireAuth();
	(new AdminController())->createUser();
	exit;
}
if (route('POST', '/api/v1/admin/assign-patient')) {
	Auth::requireAuth();
	(new AdminController())->assignPatientToDoctor();
	exit;
}
if (route('GET', '/api/v1/admin/doctors')) {
	Auth::requireAuth();
	(new AdminController())->listDoctors();
	exit;
}

// Doctor
if (route('GET', '/api/v1/doctor/overview')) {
	Auth::requireAuth();
	(new DoctorController())->overview();
	exit;
}

// Appointments
if (route('GET', '/api/v1/appointments')) {
	Auth::requireAuth();
	(new AppointmentController())->list();
	exit;
}
if (route('POST', '/api/v1/appointments')) {
	Auth::requireAuth();
	(new AppointmentController())->create();
	exit;
}
if (preg_match('#^/api/v1/appointments/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD']==='GET') {
	Auth::requireAuth();
	(new AppointmentController())->get((int)$m[1]);
	exit;
}

// Reports
if (route('GET', '/api/v1/reports')) {
	Auth::requireAuth();
	(new ReportController())->list();
	exit;
}
if (route('POST', '/api/v1/reports')) {
	Auth::requireAuth();
	(new ReportController())->create();
	exit;
}
if (preg_match('#^/api/v1/reports/(\d+)$#', $uri, $m) && $_SERVER['REQUEST_METHOD']==='GET') {
	Auth::requireAuth();
	(new ReportController())->get((int)$m[1]);
	exit;
}
// Report Notes
if (route('POST', '/api/v1/reports/notes')) {
	Auth::requireAuth();
	(new ReportNoteController())->create();
	exit;
}
if (preg_match('#^/api/v1/reports/(\d+)/notes$#', $uri, $m) && $_SERVER['REQUEST_METHOD']==='GET') {
	Auth::requireAuth();
	(new ReportNoteController())->get((int)$m[1]);
	exit;
}

// Medications
if (route('GET','/api/v1/patient-medications')) {
    Auth::requireAuth();
    (new MedicationController())->listForPatient();
    exit;
}
if (route('POST','/api/v1/patient-medications')) {
    Auth::requireAuth();
    (new MedicationController())->assign();
    exit;
}
if (preg_match('#^/api/v1/patient-medications/(\d+)$#',$uri,$m) && $_SERVER['REQUEST_METHOD']==='PATCH') {
    Auth::requireAuth();
    (new MedicationController())->setActive((int)$m[1]);
    exit;
}
if (route('POST','/api/v1/medication-logs')) {
    Auth::requireAuth();
    (new MedicationController())->logIntake();
    exit;
}

// Rehab
if (route('GET','/api/v1/rehab-plans')) {
    Auth::requireAuth();
    (new RehabController())->listForPatient();
    exit;
}
if (route('POST','/api/v1/rehab-plans')) {
    Auth::requireAuth();
    (new RehabController())->createPlan();
    exit;
}
if (preg_match('#^/api/v1/rehab-plans/(\d+)$#',$uri,$m) && $_SERVER['REQUEST_METHOD']==='GET') {
    Auth::requireAuth();
    (new RehabController())->getPlan((int)$m[1]);
    exit;
}

// Notifications
if (route('GET','/api/v1/notifications')) {
    Auth::requireAuth();
    (new NotificationController())->listMine();
    exit;
}
if (preg_match('#^/api/v1/notifications/(\d+)/read$#',$uri,$m) && $_SERVER['REQUEST_METHOD']==='POST') {
    Auth::requireAuth();
    (new NotificationController())->markRead((int)$m[1]);
    exit;
}

// Education
if (route('GET','/api/v1/education/articles')) {
    (new EducationController())->list();
    exit;
}
if (preg_match('#^/api/v1/education/articles/([A-Za-z0-9_-]+)$#',$uri,$m) && $_SERVER['REQUEST_METHOD']==='GET') {
    (new EducationController())->getBySlug($m[1]);
    exit;
}

// Symptoms
if (route('GET','/api/v1/symptoms')) {
    Auth::requireAuth();
    (new SymptomController())->list();
    exit;
}
if (route('POST','/api/v1/symptoms')) {
    Auth::requireAuth();
    (new SymptomController())->create();
    exit;
}

// Metrics
if (route('GET','/api/v1/health-metrics')) {
    Auth::requireAuth();
    (new MetricController())->list();
    exit;
}
if (route('POST','/api/v1/health-metrics')) {
    Auth::requireAuth();
    (new MetricController())->create();
    exit;
}

// Settings
if (route('GET','/api/v1/settings')) {
    Auth::requireAuth();
    (new SettingsController())->getMine();
    exit;
}
if (route('PUT','/api/v1/settings')) {
    Auth::requireAuth();
    (new SettingsController())->putMine();
    exit;
}

Response::json([
	'success' => false,
	'error' => [
		'code' => 'NOT_FOUND',
		'message' => 'Endpoint not found'
	]
], 404);


