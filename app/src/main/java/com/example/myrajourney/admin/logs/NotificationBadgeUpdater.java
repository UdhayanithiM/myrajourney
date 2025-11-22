package com.example.myrajourney.admin.logs;

import android.view.View;
import android.widget.TextView;

// --- ADDED IMPORTS ---
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Notification;
// ---------------------

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationBadgeUpdater {
    public static void update(android.content.Context ctx, TextView badgeView) {
        if (badgeView == null) return;

        ApiService api = ApiClient.getApiService(ctx);
        // Fetch unread notifications (assuming the boolean 'true' in getNotifications filters for unread)
        Call<ApiResponse<List<Notification>>> call = api.getNotifications(1, 100, true); // Increased limit to get accurate count if no meta

        call.enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Notification>>> call, Response<ApiResponse<List<Notification>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Notification> data = response.body().getData();
                    // Fix: Use list size directly since getMeta() doesn't exist in your ApiResponse
                    int count = (data != null) ? data.size() : 0;

                    if (count > 0) {
                        badgeView.setText(String.valueOf(count));
                        badgeView.setVisibility(View.VISIBLE);
                    } else {
                        badgeView.setVisibility(View.GONE);
                    }
                } else {
                    badgeView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                badgeView.setVisibility(View.GONE);
            }
        });
    }
}