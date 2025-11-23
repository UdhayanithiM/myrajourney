package com.example.myrajourney.patient.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.data.model.Report;

import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_PATIENT = 1;
    private static final int VIEW_DOCTOR = 2;

    private final Context context;
    private List<Report> reports;

    public interface OnReportClickListener {
        void onReportClick(Report report);
    }

    private OnReportClickListener clickListener;

    public void setOnReportClickListener(OnReportClickListener listener) {
        this.clickListener = listener;
    }

    public ReportsAdapter(Context context, List<Report> reports) {
        this.context = context;
        this.reports = reports;
    }

    @Override
    public int getItemViewType(int position) {
        Report r = reports.get(position);

        // If report has a file URL → patient uploaded report
        if (r.getFileUrl() != null && !r.getFileUrl().isEmpty()) {
            return VIEW_PATIENT;
        }
        return VIEW_DOCTOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_PATIENT) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_report_patient, parent, false);
            return new PatientViewHolder(view);
        } else {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_report_doctor, parent, false);
            return new DoctorViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Report report = reports.get(position);

        if (holder instanceof PatientViewHolder) {
            PatientViewHolder h = (PatientViewHolder) holder;
            h.reportName.setText(report.getTitle());
            h.reportDate.setText(report.getCreatedAt());
            h.reportFile.setText(report.getFileUrl());

            h.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onReportClick(report);
            });

        } else if (holder instanceof DoctorViewHolder) {
            DoctorViewHolder h = (DoctorViewHolder) holder;

            h.patientName.setText(report.getPatientName());
            h.reportType.setText(report.getTitle());
            h.date.setText(report.getCreatedAt());

            String status = report.getStatus() != null ? report.getStatus() : "Pending";
            h.status.setText(status);

            // Color based on status
            if (status.equalsIgnoreCase("Normal") || status.equalsIgnoreCase("Reviewed")) {
                h.status.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else if (status.equalsIgnoreCase("Abnormal") || status.equalsIgnoreCase("Action Required")) {
                h.status.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                h.status.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            }

            h.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onReportClick(report);
            });
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    // ---------------------------
    // Patient ViewHolder
    // ---------------------------
    static class PatientViewHolder extends RecyclerView.ViewHolder {

        TextView reportName, reportDate, reportFile;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            reportName = itemView.findViewById(R.id.report_name);
            reportDate = itemView.findViewById(R.id.report_date);
            reportFile = itemView.findViewById(R.id.report_file);
        }
    }

    // ---------------------------
    // Doctor ViewHolder
    // ---------------------------
    static class DoctorViewHolder extends RecyclerView.ViewHolder {

        TextView patientName, reportType, date, status;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patient_name);
            reportType = itemView.findViewById(R.id.report_type);
            date = itemView.findViewById(R.id.date);
            status = itemView.findViewById(R.id.status);
        }
    }
}
