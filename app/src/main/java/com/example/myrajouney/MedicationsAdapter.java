package com.example.myrajouney;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicationsAdapter extends RecyclerView.Adapter<MedicationsAdapter.MedicationsViewHolder> {

    private Context context;
    private List<Medication> medicationList;

    public MedicationsAdapter(Context context, List<Medication> medicationList) {
        this.context = context;
        this.medicationList = medicationList;
    }

    @NonNull
    @Override
    public MedicationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new MedicationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationsViewHolder holder, int position) {
        Medication med = medicationList.get(position);

        holder.name.setText(med.getName());
        holder.type.setText(med.getType());
        holder.details.setText(med.getDosage() + ", " + med.getFrequency() + ", " + med.getDuration());
        holder.status.setText(med.getStatus());
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public void filterList(List<Medication> filteredList) {
        this.medicationList = filteredList;
        notifyDataSetChanged();
    }

    static class MedicationsViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, details, status;

        public MedicationsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.med_name);
            type = itemView.findViewById(R.id.med_type);
            details = itemView.findViewById(R.id.med_details);
            status = itemView.findViewById(R.id.med_status);
        }
    }
}
