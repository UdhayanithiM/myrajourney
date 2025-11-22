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

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Patient;
// ---------------------

public class PatientsAdapter extends RecyclerView.Adapter<PatientsAdapter.PatientViewHolder> {

    private Context context;
    private List<Patient> patientList;

    // Constructor
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

        // Bind data
        // Ensure safely handling nulls
        String name = patient.getName() != null ? patient.getName() : "Unknown";
        holder.name.setText(name);

        // Construct details string (e.g., "Age: 30 | email@example.com")
        StringBuilder detailsBuilder = new StringBuilder();
        if (patient.getAge() != null && !patient.getAge().isEmpty()) {
            detailsBuilder.append("Age: ").append(patient.getAge());
        }
        if (patient.getEmail() != null && !patient.getEmail().isEmpty()) {
            if (detailsBuilder.length() > 0) detailsBuilder.append(" | ");
            detailsBuilder.append(patient.getEmail());
        }
        holder.details.setText(detailsBuilder.toString());

        // Set image - Using default since API might send string URL but we need a placeholder for now
        // If you have Glide/Picasso, you can load patient.getProfileImageUrl() here.
        holder.image.setImageResource(R.drawable.ic_person_default);

        // Handle click to open patient details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PatientDetailsActivity.class);
            intent.putExtra("patient_name", patient.getName());
            intent.putExtra("patient_age", patient.getAge());
            intent.putExtra("patient_email", patient.getEmail()); // Useful for details
            // Passing default image resource for now
            intent.putExtra("patient_image", R.drawable.ic_person_default);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    // ViewHolder
    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView name, details;
        ImageView image;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.patient_name);
            // Ensure this ID matches your item_patient.xml (often id/patient_details or id/patient_age_condition)
            details = itemView.findViewById(R.id.patient_age_condition);
            image = itemView.findViewById(R.id.patient_image);
        }
    }
}