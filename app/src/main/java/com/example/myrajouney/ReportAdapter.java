package com.example.myrajouney;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reportList;

    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_item, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.name.setText(report.getName());
        holder.date.setText(report.getDate());
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public void filterList(List<Report> filteredList) {
        this.reportList = filteredList;
        notifyDataSetChanged();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView name, date;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.reportName);
            date = itemView.findViewById(R.id.reportDate);
        }
    }
}
