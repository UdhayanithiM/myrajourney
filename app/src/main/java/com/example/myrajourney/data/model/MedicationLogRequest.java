package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class MedicationLogRequest {
    @SerializedName("patient_medication_id")
    private int patientMedicationId;

    @SerializedName("taken_at")
    private String takenAt;

    @SerializedName("dosage")
    private String dosage;

    @SerializedName("status")
    private String status;

    public void setPatientMedicationId(int patientMedicationId) {
        this.patientMedicationId = patientMedicationId;
    }

    public void setTakenAt(String takenAt) {
        this.takenAt = takenAt;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}