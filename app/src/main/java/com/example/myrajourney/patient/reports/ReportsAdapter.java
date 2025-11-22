package com.example.myrajourney.patient.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Report;
// ---------------------

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

        // Logic: If file URL exists, treat as Patient Report. Otherwise, Doctor Report logic.
        boolean isPatientReport = (report.getFileUrl() != null && !report.getFileUrl().isEmpty());

        if (isPatientReport) {
            // Patient View Binding
            if (holder.reportName != null) holder.reportName.setText(report.getTitle());
            if (holder.reportDate != null) holder.reportDate.setText(report.getCreatedAt());
            if (holder.reportFile != null) holder.reportFile.setText(report.getFileUrl());
            if (holder.patientName != null) holder.patientName.setText("Patient Report");
        } else {
            // Doctor View Binding
            if (holder.patientName != null)
                holder.patientName.setText(report.getPatientName() != null ? report.getPatientName() : "Unknown Patient");

            if (holder.reportType != null) holder.reportType.setText(report.getTitle());
            if (holder.date != null) holder.date.setText(report.getCreatedAt());

            if (holder.status != null) {
                String status = report.getStatus() != null ? report.getStatus() : "Pending";
                holder.status.setText(status);

                // Set status color
                if ("Normal".equalsIgnoreCase(status) || "Reviewed".equalsIgnoreCase(status)) {
                    holder.status.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                } else if ("Abnormal".equalsIgnoreCase(status) || "Action Required".equalsIgnoreCase(status)) {
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
        // Fields for both possible layouts (Patient vs Doctor item)
        TextView reportName, reportDate, reportFile; // Patient Layout fields
        TextView patientName, reportType, date, status; // Doctor Layout fields

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Use findViewById for all potential IDs. If the ID isn't in the layout, it returns null.
            // Safe null-checks are handled in onBindViewHolder.
            reportName = itemView.findViewById(R.id.report_name);
            reportDate = itemView.findViewById(R.id.report_date);
            reportFile = itemView.findViewById(R.id.report_file);

            patientName = itemView.findViewById(R.id.patient_name);
            reportType = itemView.findViewById(R.id.report_type);
            date = itemView.findViewById(R.id.date); // Some layouts use 'date', others 'report_date'
            status = itemView.findViewById(R.id.status);
        }
    }
}