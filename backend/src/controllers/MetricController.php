<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\MetricModel;
use Src\Utils\Response;

class MetricController
{
	private MetricModel $metrics;
	public function __construct(){ $this->metrics = new MetricModel(); }

	public function list(): void
	{
		$pid=(int)($_GET['patient_id'] ?? 0);
		$type=$_GET['metric_type'] ?? null;
		$from=$_GET['from'] ?? null;
		$to=$_GET['to'] ?? null;
		$data=$this->metrics->list($pid,$type,$from,$to);
		Response::json(['success'=>true,'data'=>$data]);
	}

	public function create(): void
	{
		$body=json_decode(file_get_contents('php://input'), true) ?? [];
		foreach(['patient_id','metric_type','metric_value','recorded_at'] as $k){ if(empty($body[$k])){ Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>"Missing $k"]],422); return; } }
		$id=$this->metrics->create($body);
		Response::json(['success'=>true,'data'=>['id'=>$id]],201);
	}
}




















