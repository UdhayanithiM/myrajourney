package com.example.myrajouney.patient.appointments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppViewHolder> {

    private Context context;
    private List<Appointment> appointments;
    private Appointment nextAppointment; // optional highlight

    public AppointmentAdapter(Context context, List<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    public AppointmentAdapter(Context context, List<Appointment> appointments, Appointment nextAppointment) {
        this.context = context;
        this.appointments = appointments;
        this.nextAppointment = nextAppointment;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        Appointment app = appointments.get(position);
        
        // Use new layout IDs
        if (app.getDoctorName() != null) {
            holder.patientName.setText(app.getDoctorName());
        } else if (app.getPatientName() != null) {
            holder.patientName.setText(app.getPatientName());
        }
        
        holder.date.setText(app.getDate());
        holder.time.setText(app.getTimeSlot());
        
        if (app.getAppointmentType() != null) {
            holder.type.setText(app.getAppointmentType());
        } else if (app.getReason() != null) {
            holder.type.setText(app.getReason());
        }

        // Highlight next appointment
        if (app == nextAppointment) {
            holder.itemView.setBackgroundColor(0xFFE0F7FA); // light cyan
        } else {
            holder.itemView.setBackgroundColor(0x00000000); // transparent
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        TextView patientName, date, time, type;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patient_name);
            date = itemView.findViewById(R.id.appointment_date);
            time = itemView.findViewById(R.id.appointment_time);
            type = itemView.findViewById(R.id.appointment_type);
        }
    }
}
