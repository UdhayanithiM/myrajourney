package com.example.myrajouney;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class CreatePatientActivity extends AppCompatActivity {

    private EditText etName, etMobile, etAge, etEmail, etAddress;
    private TextView tvPatientId, tvUsername, tvPassword;
    private ImageView imgProfilePic;
    private Button btnUploadPic, btnRegisterPatient;

    private static int patientCounter = 1000; // For generating unique IDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_patient);

        // Initialize Views
        imgProfilePic = findViewById(R.id.imgProfilePic);
        btnUploadPic = findViewById(R.id.btnUploadPic);

        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);

        tvPatientId = findViewById(R.id.tvPatientId);
        tvUsername = findViewById(R.id.tvUsername);
        tvPassword = findViewById(R.id.tvPassword);

        btnRegisterPatient = findViewById(R.id.btnRegisterPatient);

        // Handle Register Button
        btnRegisterPatient.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() || mobile.isEmpty() || age.isEmpty() || email.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate Patient ID and Username
            String patientId = "PAT" + patientCounter++;
            String username = "PD" + String.format("%05d", new Random().nextInt(99999));

            tvPatientId.setText("Patient ID: " + patientId);
            tvUsername.setText("Username: " + username);
            tvPassword.setText("Default Password: welcome123");

            Toast.makeText(this, "Patient Registered Successfully!", Toast.LENGTH_LONG).show();
        });
    }
}
