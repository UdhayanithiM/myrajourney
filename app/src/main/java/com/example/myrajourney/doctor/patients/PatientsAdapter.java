package com.example.myrajourney.doctor.patients;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Patient;

public class PatientsAdapter extends RecyclerView.Adapter<PatientsAdapter.PatientViewHolder> {

    private Context context;
    private List<Patient> patientList;

    public PatientsAdapter(Context context, List<Patient> patientList) {
        this.context = context;
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);

        String name = (patient.getName() != null) ? patient.getName() : "Unknown";
        holder.name.setText(name);

        // Display logic for the list item
        String age = patient.getAge();
        StringBuilder details = new StringBuilder();
        if (age != null && !age.equals("N/A")) {
            details.append("Age: ").append(age);
        } else {
            details.append("Age: --");
        }

        if (patient.getEmail() != null) {
            details.append(" | ").append(patient.getEmail());
        }
        holder.details.setText(details.toString());
        holder.image.setImageResource(R.drawable.ic_person_default);

        // ✅ CRITICAL FIX: Passing the age to the details activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PatientDetailsActivity.class);
            intent.putExtra("patient_id", patient.getId());
            intent.putExtra("patient_name", name);
            intent.putExtra("patient_age", age); // Passing value here
            intent.putExtra("patient_email", patient.getEmail());
            intent.putExtra("patient_image", R.drawable.ic_person_default);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView name, details;
        ImageView image;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.patient_name);
            details = itemView.findViewById(R.id.patient_age_condition);
            image = itemView.findViewById(R.id.patient_image);
        }
    }
}