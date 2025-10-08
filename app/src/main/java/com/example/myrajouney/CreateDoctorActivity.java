package com.example.myrajouney;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class CreateDoctorActivity extends AppCompatActivity {

    private EditText etName, etMobile, etAge, etEmail, etAddress, etDoctorId;
    private ImageView imgProfilePic;
    private Button btnUploadPic, btnRegisterDoctor;
    private TextView tvCredentials;

    private Uri imageUri;
    private String generatedUsername, defaultPassword = "doc123@saveetha";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_doctor);

        // Initialize views
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etDoctorId = findViewById(R.id.etDoctorId);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        btnUploadPic = findViewById(R.id.btnUploadPic);
        btnRegisterDoctor = findViewById(R.id.btnRegisterDoctor);
        tvCredentials = findViewById(R.id.tvCredentials);

        // Image picker
        ActivityResultLauncher<String> pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imgProfilePic.setImageURI(uri);
                    }
                });

        btnUploadPic.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Register button click
        btnRegisterDoctor.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String doctorId = etDoctorId.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(mobile) ||
                    TextUtils.isEmpty(age) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(address) || TextUtils.isEmpty(doctorId)) {
                Toast.makeText(CreateDoctorActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri == null) {
                Toast.makeText(CreateDoctorActivity.this, "Please upload profile picture", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate username
            generatedUsername = "DOC" + (10000 + new Random().nextInt(90000));

            // ✅ Show success popup
            Toast.makeText(CreateDoctorActivity.this, "Doctor Registered Successfully!", Toast.LENGTH_SHORT).show();

            // ✅ Display credentials below button
            tvCredentials.setText("Username: " + generatedUsername + "\nPassword: " + defaultPassword);
            tvCredentials.setVisibility(TextView.VISIBLE);
            
            // Show credentials card
            findViewById(R.id.credentialsCard).setVisibility(View.VISIBLE);
        });
    }
}
