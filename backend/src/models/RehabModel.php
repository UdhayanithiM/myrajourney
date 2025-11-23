<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class RehabModel
{
    private PDO $db;

    public function __construct()
    {
        $this->db = DB::conn();
    }

    /**
     * List all plans for a patient
     */
    public function plans(int $patientId): array
    {
        $stmt = $this->db->prepare(
            'SELECT * FROM rehab_plans WHERE patient_id = :pid ORDER BY updated_at DESC'
        );
        $stmt->execute([':pid' => $patientId]);

        return $stmt->fetchAll();
    }

    /**
     * Create rehab plan
     */
    public function createPlan(array $d): int
    {
        $stmt = $this->db->prepare(
            'INSERT INTO rehab_plans (patient_id, title, description, created_at, updated_at)
             VALUES (:pid, :title, :description, NOW(), NOW())'
        );

        $stmt->execute([
            ':pid' => (int)$d['patient_id'],
            ':title' => $d['title'],
            ':description' => $d['description'] ?? null
        ]);

        return (int)$this->db->lastInsertId();
    }

    /**
     * Add exercises to a plan
     */
    public function addExercises(int $planId, array $exercises): void
    {
        $stmt = $this->db->prepare(
            'INSERT INTO rehab_exercises
            (rehab_plan_id, name, description, reps, sets, frequency_per_week)
            VALUES (:rid, :name, :desc, :reps, :sets, :fpw)'
        );

        foreach ($exercises as $ex) {
            $stmt->execute([
                ':rid' => $planId,
                ':name' => $ex['name'],
                ':desc' => $ex['description'] ?? '',
                ':reps' => $ex['reps'] ?? null,
                ':sets' => $ex['sets'] ?? null,
                ':fpw' => $ex['frequency_per_week'] ?? null,
            ]);
        }
    }

    /**
     * Fetch plan with exercises
     */
    public function planWithExercises(int $id): ?array
    {
        // Fetch plan
        $p = $this->db->prepare('SELECT * FROM rehab_plans WHERE id = :id');
        $p->execute([':id' => $id]);
        $plan = $p->fetch();

        if (!$plan) {
            return null;
        }

        // Fetch exercises
        $e = $this->db->prepare('SELECT * FROM rehab_exercises WHERE rehab_plan_id = :id');
        $e->execute([':id' => $id]);   // FIXED HERE ✔✔✔

        $plan['exercises'] = $e->fetchAll();

        return $plan;
    }
}
