package com.example.myrajourney.patient.rehab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Rehab; // Forces use of correct model
// ---------------------

import java.util.List;

public class RehabAdapter extends RecyclerView.Adapter<RehabAdapter.RehabViewHolder> {

    private Context context;
    private List<Rehab> rehabList;

    public RehabAdapter(Context context, List<Rehab> rehabList) {
        this.context = context;
        this.rehabList = rehabList;
    }

    @NonNull
    @Override
    public RehabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rehab, parent, false);
        return new RehabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RehabViewHolder holder, int position) {
        Rehab rehab = rehabList.get(position);

        holder.name.setText(rehab.getName());
        holder.description.setText(rehab.getDescription());
        holder.reps.setText(rehab.getReps());
        holder.frequency.setText(rehab.getFrequency());

        if (rehab.getThumbnailUrl() != null && !rehab.getThumbnailUrl().isEmpty()) {
            Glide.with(context)
                    .load(rehab.getThumbnailUrl())
                    .into(holder.thumbnail);
        }
    }

    @Override
    public int getItemCount() { return rehabList.size(); }

    public void filterList(List<Rehab> filteredList) {
        this.rehabList = filteredList;
        notifyDataSetChanged();
    }

    static class RehabViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, reps, frequency;
        ImageView thumbnail;

        public RehabViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.rehab_name);
            description = itemView.findViewById(R.id.rehab_description);
            reps = itemView.findViewById(R.id.rehab_reps);
            frequency = itemView.findViewById(R.id.rehab_frequency);
            thumbnail = itemView.findViewById(R.id.rehab_thumbnail);
        }
    }
}