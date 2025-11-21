package com.example.myrajouney.admin.assignments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajouney.api.models.Doctor;
import com.example.myrajouney.api.models.User;

import java.util.ArrayList;
import java.util.List;

public class AssignPatientToDoctorActivity extends AppCompatActivity {

    private RecyclerView patientsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;

    private List<User> patientsList = new ArrayList<>();
    private List<Doctor> doctorsList = new ArrayList<>();
    private PatientAssignmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_patient_to_doctor);

        // Initialize views
        patientsRecyclerView = findViewById(R.id.patientsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        // Setup RecyclerView
        patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load data
        loadDoctors();
        loadPatients();
    }

    private void loadDoctors() {
        String baseIp = getString(R.string.api_base_ip);
        String url = "http://" + baseIp + "/myra-admin.php?action=users";

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(AssignPatientToDoctorActivity.this,
                        "Failed to load doctors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseData);
                        org.json.JSONArray users = json.getJSONArray("data");

                        doctorsList = new ArrayList<>();
                        for (int i = 0; i < users.length(); i++) {
                            org.json.JSONObject user = users.getJSONObject(i);
                            if ("DOCTOR".equals(user.optString("role"))) {
                                Doctor doctor = new Doctor();
                                doctor.setId(user.getInt("id"));
                                doctor.setName(user.optString("name"));
                                doctor.setEmail(user.optString("email"));
                                doctor.setSpecialization(user.optString("specialization"));
                                doctorsList.add(doctor);
                            }
                        }

                        runOnUiThread(() -> Toast.makeText(AssignPatientToDoctorActivity.this,
                                "Loaded " + doctorsList.size() + " doctors", Toast.LENGTH_SHORT).show());
                    } catch (org.json.JSONException e) {
                        runOnUiThread(() -> Toast.makeText(AssignPatientToDoctorActivity.this,
                                "Error parsing doctors", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void loadPatients() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        String baseIp = getString(R.string.api_base_ip);
        String url = "http://" + baseIp + "/myra-admin.php?action=users";

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseData);
                        org.json.JSONArray users = json.getJSONArray("data");

                        patientsList = new ArrayList<>();
                        for (int i = 0; i < users.length(); i++) {
                            org.json.JSONObject userJson = users.getJSONObject(i);
                            if ("PATIENT".equals(userJson.optString("role"))) {
                                User user = new User();
                                user.setId(userJson.getInt("id"));
                                user.setName(userJson.optString("name"));
                                user.setEmail(userJson.optString("email"));
                                user.setRole("PATIENT");
                                user.setAssignedDoctorId(userJson.isNull("assigned_doctor_id") ?
                                        null : userJson.getInt("assigned_doctor_id"));
                                patientsList.add(user);
                            }
                        }

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (patientsList.isEmpty()) {
                                emptyView.setVisibility(View.VISIBLE);
                                emptyView.setText("No patients found");
                            } else {
                                adapter = new PatientAssignmentAdapter(patientsList, doctorsList,
                                        AssignPatientToDoctorActivity.this::assignPatient);
                                patientsRecyclerView.setAdapter(adapter);
                            }
                        });
                    } catch (org.json.JSONException e) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText("Error parsing data");
                        });
                    }
                }
            }
        });
    }

    private void assignPatient(int patientId, Integer doctorId) {
        String baseIp = getString(R.string.api_base_ip);
        String url = "http://" + baseIp + "/myra-admin.php?action=assign";

        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("patient_id", patientId);
            jsonBody.put("doctor_id", doctorId);
        } catch (org.json.JSONException e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        okhttp3.MediaType JSON = okhttp3.MediaType.get("application/json; charset=utf-8");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody.toString(), JSON);

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(AssignPatientToDoctorActivity.this,
                        "Assignment failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String responseData = response.body().string();
                runOnUiThread(() -> {
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseData);
                        if (json.optBoolean("success")) {
                            Toast.makeText(AssignPatientToDoctorActivity.this,
                                    "Patient assigned successfully!", Toast.LENGTH_LONG).show();
                            loadPatients();
                        } else {
                            Toast.makeText(AssignPatientToDoctorActivity.this,
                                    "Assignment failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (org.json.JSONException e) {
                        Toast.makeText(AssignPatientToDoctorActivity.this,
                                "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
