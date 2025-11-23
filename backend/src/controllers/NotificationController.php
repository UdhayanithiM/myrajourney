<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\NotificationModel;
use Src\Utils\Response;

class NotificationController
{
    private NotificationModel $notifs;

    public function __construct()
    {
        $this->notifs = new NotificationModel();
    }

    /**
     * List notifications for logged-in user
     */
    public function listMine(): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $uid = (int)($auth['uid'] ?? 0);

        if ($uid <= 0) {
            Response::json(['success' => false, 'message' => 'Unauthorized'], 401);
            return;
        }

        $page  = max(1, (int)($_GET['page'] ?? 1));
        $limit = max(1, min(100, (int)($_GET['limit'] ?? 20)));

        // unread=true | unread=false | empty → all
        $unread = isset($_GET['unread'])
            ? ($_GET['unread'] === 'true')
            : null;

        $res = $this->notifs->list($uid, $page, $limit, $unread);

        // Convert DB fields into clean JSON for Android
        $items = array_map(function($n) {
            return [
                'id'        => (string)$n['id'],
                'title'     => $n['title'],
                'message'   => $n['message'],     // Android matches this
                'is_read'   => (bool)$n['is_read'], // FORCE BOOLEAN
                'created_at'=> $n['created_at']
            ];
        }, $res['items']);

        Response::json([
            'success' => true,
            'data'    => $items,
            'meta'    => [
                'total' => $res['total'],
                'page'  => $page,
                'limit' => $limit
            ]
        ]);
    }

    /**
     * Mark a notification as read
     */
    public function markRead(int $id): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $uid = (int)($auth['uid'] ?? 0);

        if ($uid <= 0) {
            Response::json(['success' => false, 'message' => 'Unauthorized'], 401);
            return;
        }

        $this->notifs->markRead($id, $uid);

        Response::json(['success' => true]);
    }
}
