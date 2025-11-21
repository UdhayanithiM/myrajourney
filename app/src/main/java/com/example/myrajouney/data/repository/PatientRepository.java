package com.example.myrajouney.data.repository;

import android.content.Context;

import com.example.myrajouney.core.network.ApiClient;
import com.example.myrajouney.core.network.ApiService;
import com.example.myrajouney.data.model.ApiResponse;
import com.example.myrajouney.data.model.Appointment;
import com.example.myrajouney.data.model.PatientOverview;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientRepository {

    private final ApiService apiService;

    public PatientRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public void getPatientOverview(final DataCallback<PatientOverview> callback) {
        apiService.getPatientOverview().enqueue(new Callback<ApiResponse<PatientOverview>>() {
            @Override
            public void onResponse(Call<ApiResponse<PatientOverview>> call, Response<ApiResponse<PatientOverview>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to load overview");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PatientOverview>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getAppointments(final DataCallback<List<Appointment>> callback) {
        apiService.getAppointments().enqueue(new Callback<ApiResponse<List<Appointment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Appointment>>> call, Response<ApiResponse<List<Appointment>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to load appointments");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Appointment>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Simple callback interface for the ViewModel
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}