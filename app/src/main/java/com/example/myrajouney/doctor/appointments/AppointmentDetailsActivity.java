package com.example.myrajouney.doctor.appointments;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AppointmentDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);

        ImageView back = findViewById(R.id.back_button);
        if (back != null) back.setOnClickListener(v -> finish());

        TextView title = findViewById(R.id.title);
        TextView when = findViewById(R.id.when);
        TextView details = findViewById(R.id.details);

        String t = getIntent().getStringExtra("title");
        String dt = getIntent().getStringExtra("datetime");
        String d = getIntent().getStringExtra("details");

        if (t != null) title.setText(t);
        if (dt != null) when.setText(dt);
        if (d != null) details.setText(d);
    }
}


