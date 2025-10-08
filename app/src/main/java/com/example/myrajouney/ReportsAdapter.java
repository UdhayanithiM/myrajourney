package com.example.myrajouney;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {

    private Context context;
    private List<Report> reports;

    // Add click listener interface
    public interface OnReportClickListener {
        void onReportClick(Report report);
    }

    private OnReportClickListener onReportClickListener;

    public ReportsAdapter(Context context, List<Report> reports) {
        this.context = context;
        this.reports = reports;
    }

    // Add method to set click listener
    public void setOnReportClickListener(OnReportClickListener listener) {
        this.onReportClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report report = reports.get(position);

        // Check if this is a patient report (has fileUri) or doctor report (has patientName)
        if (report.getFileUri() != null && !report.getFileUri().isEmpty()) {
            // This is a patient report
            holder.reportName.setText(report.getName());
            holder.reportDate.setText(report.getDate());
            holder.reportFile.setText(report.getFileUri());
            holder.patientName.setText("Patient Report");
        } else {
            // This is a doctor report
            holder.patientName.setText(report.getPatientName());
            holder.reportType.setText(report.getReportType());
            holder.date.setText(report.getDate());
            holder.status.setText(report.getStatus());

            // Set status color
            if (report.getStatus() != null) {
                if (report.getStatus().equals("Normal")) {
                    holder.status.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                } else if (report.getStatus().equals("Abnormal")) {
                    holder.status.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    holder.status.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                }
            }
        }

        // Make report clickable
        holder.itemView.setOnClickListener(v -> {
            if (onReportClickListener != null) {
                onReportClickListener.onReportClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView reportName, reportDate, reportFile, patientName, reportType, date, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Try to find all possible TextViews that might exist in the layout
            reportName = itemView.findViewById(R.id.report_name);
            reportDate = itemView.findViewById(R.id.report_date);
            reportFile = itemView.findViewById(R.id.report_file);
            patientName = itemView.findViewById(R.id.patient_name);
            reportType = itemView.findViewById(R.id.report_type);
            date = itemView.findViewById(R.id.date);
            status = itemView.findViewById(R.id.status);
        }
    }
}
