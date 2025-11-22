package com.example.myrajourney.core.network;

import com.example.myrajourney.data.model.ActiveRequest;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.AppointmentRequest;
import com.example.myrajourney.data.model.AuthRequest;
import com.example.myrajourney.data.model.AuthResponse;
import com.example.myrajourney.data.model.CreateUserRequest;
import com.example.myrajourney.data.model.Doctor;
import com.example.myrajourney.data.model.DoctorOverview;
import com.example.myrajourney.data.model.EducationArticle;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.MedicationLog;
import com.example.myrajourney.data.model.MedicationLogRequest;
import com.example.myrajourney.data.model.MedicationRequest;
import com.example.myrajourney.data.model.Notification;
import com.example.myrajourney.data.model.PatientOverview;
import com.example.myrajourney.data.model.RehabPlan;
import com.example.myrajourney.data.model.Report;
import com.example.myrajourney.data.model.ReportNote;
import com.example.myrajourney.data.model.Settings;
import com.example.myrajourney.data.model.SettingsRequest;
import com.example.myrajourney.data.model.Symptom;
import com.example.myrajourney.data.model.SymptomRequest;
import com.example.myrajourney.data.model.User;
import com.example.myrajourney.data.model.UserResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // --- Auth Endpoints ---
    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body AuthRequest request);

    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body AuthRequest request);

    @POST("auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body AuthRequest request);

    @POST("auth/reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body Map<String, String> request);

    @POST("auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body Map<String, String> request);

    @GET("auth/me")
    Call<ApiResponse<UserResponse>> getCurrentUser();

    // ✅ FIXED: Added the missing updateProfile method here
    @PUT("users/me")
    Call<ApiResponse<User>> updateProfile(@Body Map<String, String> fields);

    // --- Education Endpoints ---
    @GET("education/articles")
    Call<ApiResponse<List<EducationArticle>>> getEducationArticles();

    @GET("education/articles/{slug}")
    Call<ApiResponse<EducationArticle>> getEducationArticle(@Path("slug") String slug);

    // --- Patient Endpoints ---
    @GET("patients/me/overview")
    Call<ApiResponse<PatientOverview>> getPatientOverview();

    @GET("patients")
    Call<ApiResponse<List<User>>> getAllPatients();

    // --- Doctor Endpoints ---
    @GET("doctor/overview")
    Call<ApiResponse<DoctorOverview>> getDoctorOverview();

    // --- Appointment Endpoints ---
    @GET("appointments")
    Call<ApiResponse<List<Appointment>>> getAppointments();

    @POST("appointments")
    Call<ApiResponse<Appointment>> createAppointment(@Body AppointmentRequest request);

    @GET("appointments/{id}")
    Call<ApiResponse<Appointment>> getAppointment(@Path("id") String id);

    // --- Medication Endpoints ---
    @GET("patient-medications")
    Call<ApiResponse<List<Medication>>> getPatientMedications();

    @POST("patient-medications")
    Call<ApiResponse<Medication>> assignMedication(@Body MedicationRequest request);

    @PATCH("patient-medications/{id}")
    Call<ApiResponse<Void>> setMedicationActive(@Path("id") String id, @Body ActiveRequest request);

    @POST("medication-logs")
    Call<ApiResponse<MedicationLog>> logMedicationIntake(@Body MedicationLogRequest request);

    // --- Report Endpoints ---
    @GET("reports")
    Call<ApiResponse<List<Report>>> getReports();

    @POST("reports")
    @Multipart
    Call<ApiResponse<Report>> createReport(
            @Part("patient_id") RequestBody patientId,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file
    );

    @POST("reports/notes")
    Call<ApiResponse<ReportNote>> createReportNote(@Body Map<String, Object> request);

    @GET("reports/{id}/notes")
    Call<ApiResponse<List<ReportNote>>> getReportNotes(@Path("id") String reportId);

    @GET("reports/{id}")
    Call<ApiResponse<Report>> getReport(@Path("id") String id);

    // --- Notification Endpoints ---
    @GET("notifications")
    Call<ApiResponse<List<Notification>>> getNotifications(
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("unread") Boolean unread
    );

    @POST("notifications/{id}/read")
    Call<ApiResponse<Void>> markNotificationRead(@Path("id") String id);

    // --- Symptom Endpoints ---
    @GET("symptoms")
    Call<ApiResponse<List<Symptom>>> getSymptoms();

    @POST("symptoms")
    Call<ApiResponse<Symptom>> createSymptom(@Body SymptomRequest request);

    // --- Settings Endpoints ---
    @GET("settings")
    Call<ApiResponse<Settings>> getSettings();

    @PUT("settings")
    Call<ApiResponse<Settings>> updateSettings(@Body SettingsRequest request);

    // --- Rehab Plans ---
    @GET("rehab-plans")
    Call<ApiResponse<List<RehabPlan>>> getRehabPlans(@Query("patient_id") Integer patientId);

    // --- Admin Endpoints ---
    @POST("admin/users")
    Call<ApiResponse<User>> createUser(@Body CreateUserRequest request);

    @POST("admin/assign-patient")
    Call<ApiResponse<Void>> assignPatientToDoctor(@Body Map<String, Integer> request);

    @GET("admin/doctors")
    Call<ApiResponse<List<Doctor>>> getAllDoctors();
}