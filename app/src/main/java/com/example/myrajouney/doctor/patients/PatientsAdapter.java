package com.example.myrajouney.doctor.patients;

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
        holder.name.setText(patient.getName());
        holder.details.setText(patient.getDetails());
        holder.image.setImageResource(patient.getImageResId());

        // Handle click to open patient details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PatientDetailsActivity.class);
            intent.putExtra("patient_name", patient.getName());
            intent.putExtra("patient_details", patient.getDetails());
            intent.putExtra("patient_image", patient.getImageResId());
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
            details = itemView.findViewById(R.id.patient_age_condition);
            image = itemView.findViewById(R.id.patient_image);
        }
    }
}
