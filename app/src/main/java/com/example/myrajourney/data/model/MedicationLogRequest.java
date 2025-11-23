package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class MedicationLogRequest {
    @SerializedName("patient_medication_id")
    private String patientMedicationId;

    @SerializedName("taken_at")
    private String takenAt;

    @SerializedName("dosage")
    private String dosage;

    @SerializedName("status")
    private String status;

    public MedicationLogRequest(String patientMedicationId, String takenAt) {
        this.patientMedicationId = patientMedicationId;
        this.takenAt = takenAt;
        this.status = "Taken";
    }

    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setStatus(String status) { this.status = status; }
}