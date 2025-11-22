package com.example.myrajourney.patient.medications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Medication;
// ---------------------

import java.util.List;

public class MedicationsAdapter extends RecyclerView.Adapter<MedicationsAdapter.ViewHolder> {

    private Context context;
    private List<Medication> medicationList;

    public MedicationsAdapter(Context context, List<Medication> medicationList) {
        this.context = context;
        this.medicationList = medicationList;
    }

    public void filterList(List<Medication> filteredList) {
        this.medicationList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication medication = medicationList.get(position);
        holder.name.setText(medication.getName());

        // Safe checks for null values
        String dosage = medication.getDosage() != null ? medication.getDosage() : "N/A";
        String frequency = medication.getFrequency() != null ? medication.getFrequency() : "N/A";

        holder.dosage.setText(dosage);
        holder.frequency.setText(frequency);
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, dosage, frequency;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs must exist in res/layout/item_medication.xml
            name = itemView.findViewById(R.id.med_name);
            dosage = itemView.findViewById(R.id.med_dosage);
            frequency = itemView.findViewById(R.id.med_frequency);
        }
    }
}