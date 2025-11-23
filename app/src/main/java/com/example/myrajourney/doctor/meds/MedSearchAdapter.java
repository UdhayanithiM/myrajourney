package com.example.myrajourney.doctor.meds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Medication;

import java.util.List;

public class MedSearchAdapter extends RecyclerView.Adapter<MedSearchAdapter.VH> {

    public interface OnSelect {
        void onSelect(Medication med);
    }

    private final Context ctx;
    private final List<Medication> meds;
    private final OnSelect onSelect;

    public MedSearchAdapter(Context ctx, List<Medication> meds, OnSelect onSelect) {
        this.ctx = ctx;
        this.meds = meds;
        this.onSelect = onSelect;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_med_search, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Medication m = meds.get(position);
        holder.name.setText(m.getName() == null ? "Unknown" : m.getName());
        String details = (m.getDosage() != null ? m.getDosage() : "") + (m.getType() != null ? " • " + m.getType() : "");
        holder.details.setText(details.trim());
        holder.select.setOnClickListener(v -> {
            if (onSelect != null) onSelect.onSelect(m);
        });
    }

    @Override
    public int getItemCount() {
        return meds.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, details;
        Button select;
        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_med_name);
            details = itemView.findViewById(R.id.tv_med_details);
            select = itemView.findViewById(R.id.btn_select);
        }
    }
}
