package com.example.myrajouney.admin.users;

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

    private EditText etName, etMobile, etAge, etEmail, etAddress, etSpecialization;
    private ImageView imgProfilePic;
    private Button btnUploadPic, btnRegisterDoctor;
    private TextView tvCredentials;

    private Uri imageUri;
    private String defaultPassword = "welcome123";

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
        etSpecialization = findViewById(R.id.etSpecialization);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        btnUploadPic = findViewById(R.id.btnUploadPic);
        btnRegisterDoctor = findViewById(R.id.btnRegisterDoctor);
        tvCredentials = findViewById(R.id.tvCredentials);

        // Image picker (optional)
        ActivityResultLauncher<String> pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imgProfilePic.setImageURI(uri);
                    }
                });

        btnUploadPic.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Register button click
        btnRegisterDoctor.setOnClickListener(v -> createDoctor());
    }
    
    private void createDoctor() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String specialization = etSpecialization != null ? etSpecialization.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(mobile) ||
                TextUtils.isEmpty(age) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button during API call
        btnRegisterDoctor.setEnabled(false);
        btnRegisterDoctor.setText("Creating...");

        // Create doctor via backend API
        com.example.myrajouney.api.ApiService apiService = com.example.myrajouney.api.ApiClient.getApiService(this);
        
        // Create request
        com.example.myrajouney.api.models.CreateUserRequest request = new com.example.myrajouney.api.models.CreateUserRequest(
            name, email, mobile, "DOCTOR", defaultPassword, address
        );
        request.setSpecialization(specialization);
        
        retrofit2.Call<com.example.myrajouney.api.models.ApiResponse<com.example.myrajouney.api.models.User>> call = apiService.createUser(request);
        
        call.enqueue(new retrofit2.Callback<com.example.myrajouney.api.models.ApiResponse<com.example.myrajouney.api.models.User>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajouney.api.models.ApiResponse<com.example.myrajouney.api.models.User>> call, retrofit2.Response<com.example.myrajouney.api.models.ApiResponse<com.example.myrajouney.api.models.User>> response) {
                btnRegisterDoctor.setEnabled(true);
                btnRegisterDoctor.setText("Register Doctor");
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.example.myrajouney.api.models.User user = response.body().getData();
                    if (user != null) {
                        // Show success message
                        Toast.makeText(CreateDoctorActivity.this, "Doctor Registered Successfully!", Toast.LENGTH_LONG).show();
                        
                        // Display credentials
                        tvCredentials.setText("Doctor ID: " + user.getId() + "\nEmail: " + user.getEmail() + "\nDefault Password: " + defaultPassword);
                        tvCredentials.setVisibility(TextView.VISIBLE);
                        
                        // Show credentials card
                        findViewById(R.id.credentialsCard).setVisibility(View.VISIBLE);
                        
                        // Clear form
                        etName.setText("");
                        etMobile.setText("");
                        etAge.setText("");
                        etEmail.setText("");
                        etAddress.setText("");
                        if (etSpecialization != null) etSpecialization.setText("");
                    }
                } else {
                    String errorMsg = "Registration failed";
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();
                    }
                    Toast.makeText(CreateDoctorActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajouney.api.models.ApiResponse<com.example.myrajouney.api.models.User>> call, Throwable t) {
                btnRegisterDoctor.setEnabled(true);
                btnRegisterDoctor.setText("Register Doctor");
                Toast.makeText(CreateDoctorActivity.this, "Registration failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
