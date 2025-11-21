<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Utils\Response;
use Src\Models\UserModel;
use Src\Utils\Jwt;
use Src\Models\PasswordResetModel;
use Src\Utils\Mailer;

class AuthController
{
	private UserModel $users;

	public function __construct()
	{
		$this->users = new UserModel();
	}

	public function register(): void
	{
		// Registration should only be allowed by admins through admin panel
		// Public registration is disabled - users must be created by admin
		Response::json(['success'=>false,'error'=>['code'=>'REGISTRATION_DISABLED','message'=>'Registration is disabled. Please contact administrator to create an account.']], 403);
		return;
	}

	public function login(): void
	{
		$body = json_decode(file_get_contents('php://input'), true) ?? [];
		$email = trim(strtolower($body['email'] ?? ''));
		$password = (string)($body['password'] ?? '');
		$user = $this->users->findByEmail($email);
		if (!$user || !password_verify($password, $user['password_hash'])) {
			Response::json(['success'=>false,'error'=>['code'=>'INVALID_CREDENTIALS','message'=>'Invalid email or password']], 401);
			return;
		}
		// Check if user is active (only admin-created users should be able to login)
		if ($user['status'] !== 'ACTIVE') {
			Response::json(['success'=>false,'error'=>['code'=>'ACCOUNT_SUSPENDED','message'=>'Your account has been suspended. Please contact administrator.']], 403);
			return;
		}
		$this->users->setLastLogin((int)$user['id']);
		$token = $this->issueToken((int)$user['id'], $user['role']);
		unset($user['password_hash']);
		Response::json(['success'=>true,'data'=>['token'=>$token,'user'=>$user]]);
	}

	public function me(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$user = $this->users->findById((int)($auth['uid'] ?? 0));
		Response::json(['success'=>true,'data'=>['user'=>$user]]);
	}

	private function issueToken(int $uid, string $role): string
	{
		$ttl = (int)($_ENV['JWT_TTL_SECONDS'] ?? 604800);
		$payload = [
			'uid' => $uid,
			'role' => $role,
			'iat' => time(),
			'exp' => time() + $ttl,
		];
		return Jwt::encode($payload, $_ENV['JWT_SECRET'] ?? '');
	}

    public function forgotPassword(): void
    {
        $body = json_decode(file_get_contents('php://input'), true) ?? [];
        $email = trim(strtolower($body['email'] ?? ''));
        if (!$email) { Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Email required']],422); return; }
        $user = $this->users->findByEmail($email);
        if (!$user) { Response::json(['success'=>true]); return; } // do not leak existence
        $token = bin2hex(random_bytes(16));
        $ttl = 3600; // 1 hour
        (new PasswordResetModel())->createToken((int)$user['id'], $token, $ttl);
        $appUrl = rtrim($_ENV['APP_URL'] ?? 'http://localhost', '/');
        $resetLink = $appUrl.'/reset-password?token='.$token;
        $sent = Mailer::send($email, 'Password Reset', '<p>Use the link to reset your password:</p><p><a href="'.$resetLink.'">Reset Password</a></p>');
        Response::json(['success'=>true]);
    }

    public function resetPassword(): void
    {
        $body = json_decode(file_get_contents('php://input'), true) ?? [];
        $token = (string)($body['token'] ?? '');
        $newPass = (string)($body['password'] ?? '');
        $email = trim(strtolower($body['email'] ?? ''));
        
        // Validate email format
        if ($email && !filter_var($email, FILTER_VALIDATE_EMAIL)) {
            Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Invalid email format']],422);
            return;
        }
        
        // Validate password length (minimum 8 characters)
        if (strlen($newPass) < 8) {
            Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Password must be at least 8 characters long']],422);
            return;
        }
        
        // If token is provided, use token-based reset
        if ($token) {
            $pr = new PasswordResetModel();
            $row = $pr->findValid($token);
            if (!$row) {
                Response::json(['success'=>false,'error'=>['code'=>'INVALID_TOKEN','message'=>'Token invalid or expired']],400);
                return;
            }
            // Update password using token
            $this->users->updatePassword((int)$row['user_id'], password_hash($newPass, PASSWORD_BCRYPT));
            $pr->consume((int)$row['id']);
            Response::json(['success'=>true,'message'=>'Password reset successfully']);
            return;
        }
        
        // If no token, allow reset with email verification (for same-page reset)
        if ($email) {
            $user = $this->users->findByEmail($email);
            if (!$user) {
                Response::json(['success'=>false,'error'=>['code'=>'INVALID_EMAIL','message'=>'Email not found']],404);
                return;
            }
            // Update password directly
            $this->users->updatePassword((int)$user['id'], password_hash($newPass, PASSWORD_BCRYPT));
            Response::json(['success'=>true,'message'=>'Password updated successfully']);
            return;
        }
        
        // Neither token nor email provided
        Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Token or email required']],422);
    }

    public function changePassword(): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $uid = (int)($auth['uid'] ?? 0);
        
        if ($uid <= 0) {
            Response::json(['success'=>false,'error'=>['code'=>'UNAUTHORIZED','message'=>'Authentication required']],401);
            return;
        }
        
        $body = json_decode(file_get_contents('php://input'), true) ?? [];
        $oldPassword = (string)($body['old_password'] ?? '');
        $newPassword = (string)($body['new_password'] ?? '');
        
        // Validate inputs
        if (!$oldPassword || !$newPassword) {
            Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Old and new passwords are required']],422);
            return;
        }
        
        if (strlen($newPassword) < 6) {
            Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'New password must be at least 6 characters']],422);
            return;
        }
        
        // Get user
        $user = $this->users->findById($uid);
        if (!$user) {
            Response::json(['success'=>false,'error'=>['code'=>'NOT_FOUND','message'=>'User not found']],404);
            return;
        }
        
        // Verify old password
        if (!password_verify($oldPassword, $user['password_hash'])) {
            Response::json(['success'=>false,'error'=>['code'=>'INVALID_PASSWORD','message'=>'Current password is incorrect']],401);
            return;
        }
        
        // Update password
        $this->users->updatePassword($uid, password_hash($newPassword, PASSWORD_BCRYPT));
        
        Response::json(['success'=>true,'message'=>'Password changed successfully']);
    }
}






