package com.example.myrajouney.common.rehab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AddRehabAdapter extends RecyclerView.Adapter<AddRehabAdapter.AddRehabViewHolder> {

    private Context context;
    private List<Rehab> rehabList;

    public AddRehabAdapter(Context context, List<Rehab> rehabList) {
        this.context = context;
        this.rehabList = rehabList;
    }

    @NonNull
    @Override
    public AddRehabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_add_rehab, parent, false);
        return new AddRehabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddRehabViewHolder holder, int position) {
        Rehab rehab = rehabList.get(position);

        holder.name.setText(rehab.getName());
        holder.description.setText(rehab.getDescription());
        holder.reps.setText(rehab.getReps());
        holder.frequency.setText(rehab.getFrequency());

        Glide.with(context)
                .load(rehab.getThumbnailUrl())
                .into(holder.thumbnail);

        holder.checkBox.setChecked(rehab.isSelected());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> rehab.setSelected(isChecked));
    }

    @Override
    public int getItemCount() { return rehabList.size(); }

    public void filterList(List<Rehab> filteredList) {
        this.rehabList = filteredList;
        notifyDataSetChanged();
    }

    static class AddRehabViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, reps, frequency;
        ImageView thumbnail;
        CheckBox checkBox;

        public AddRehabViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.rehab_name);
            description = itemView.findViewById(R.id.rehab_description);
            reps = itemView.findViewById(R.id.rehab_reps);
            frequency = itemView.findViewById(R.id.rehab_frequency);
            thumbnail = itemView.findViewById(R.id.rehab_thumbnail);
            checkBox = itemView.findViewById(R.id.rehab_checkbox);
        }
    }
}
