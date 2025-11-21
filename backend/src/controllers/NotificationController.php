<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\NotificationModel;
use Src\Utils\Response;

class NotificationController
{
	private NotificationModel $notifs;
	public function __construct(){ $this->notifs = new NotificationModel(); }

	public function listMine(): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$page = max(1,(int)($_GET['page'] ?? 1));
		$limit = max(1, min(100,(int)($_GET['limit'] ?? 20)));
		$unread = isset($_GET['unread']) ? ($_GET['unread'] === 'true') : null;
		$r = $this->notifs->list($uid, $page, $limit, $unread);
		Response::json(['success'=>true,'data'=>$r['items'],'meta'=>['total'=>$r['total'],'page'=>$page,'limit'=>$limit]]);
	}

	public function markRead(int $id): void
	{
		$auth = $_SERVER['auth'] ?? [];
		$uid = (int)($auth['uid'] ?? 0);
		$this->notifs->markRead($id, $uid);
		Response::json(['success'=>true]);
	}
}




















