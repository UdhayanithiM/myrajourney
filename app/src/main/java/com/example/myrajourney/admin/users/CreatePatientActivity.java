package com.example.myrajourney.admin.users;

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
        btnRegisterPatient.setOnClickListener(v -> createPatient());
    }
    
    private void createPatient() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || mobile.isEmpty() || age.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create patient via backend API
        com.example.myrajourney.core.network.ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        
        // Create request
        com.example.myrajourney.data.model.CreateUserRequest request = new com.example.myrajourney.data.model.CreateUserRequest(
            name, email, mobile, "PATIENT", "welcome123", address
        );
        
        retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.User>> call = apiService.createUser(request);
        
        call.enqueue(new retrofit2.Callback<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.User>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myrajourney.data.model.ApiResponse<com.example.myrajourney.data.model.User>> call, retrofit2.Response<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.example.myrajourney.data.model
.User user = response.body().getData();
                    if (user != null) {
                        tvPatientId.setText("Patient ID: " + user.getId());
                        tvUsername.setText("Username: " + user.getEmail());
                        tvPassword.setText("Default Password: welcome123");
                        Toast.makeText(CreatePatientActivity.this, "Patient Registered Successfully!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = "Registration failed";
                    if (response.body() != null && response.body().getError() != null) {
                        errorMsg = response.body().getError().getMessage();
                    }
                    Toast.makeText(CreatePatientActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.myrajourney.data.model
.ApiResponse<com.example.myrajourney.data.model
.User>> call, Throwable t) {
                Toast.makeText(CreatePatientActivity.this, "Registration failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}






