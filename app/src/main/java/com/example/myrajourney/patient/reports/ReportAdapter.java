package com.example.myrajourney.patient.reports;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Report;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private Context context;
    private List<Report> reportList;

    public ReportAdapter(Context context, List<Report> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    // Constructor compatible with just list if Context isn't strictly needed for layout inflater
    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        // Using getters from the Report model
        holder.reportName.setText(report.getName());
        holder.reportDate.setText(report.getDate());
        holder.reportFile.setText(report.getFileUri() != null ? report.getFileUri() : "No file attached");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailsActivity.class);
            intent.putExtra("report_name", report.getName());
            intent.putExtra("report_date", report.getDate());
            intent.putExtra("report_file", report.getFileUri());
            context.startActivity(intent);
        });
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
        TextView reportName, reportDate, reportFile;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportName = itemView.findViewById(R.id.report_name);
            reportDate = itemView.findViewById(R.id.report_date);
            reportFile = itemView.findViewById(R.id.report_file);
        }
    }
}