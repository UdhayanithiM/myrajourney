package com.example.myrajouney.admin.logs;

import android.view.View;
import android.widget.TextView;
import com.example.myrajouney.api.ApiService;
import com.example.myrajouney.api.models.ApiResponse;
import com.example.myrajouney.api.models.Notification;
import java.util.List;

public class NotificationBadgeUpdater {
    public static void update(android.content.Context ctx, TextView badgeView) {
        if (badgeView == null) return;
        ApiService api = com.example.myrajouney.api.ApiClient.getApiService(ctx);
        retrofit2.Call<ApiResponse<List<Notification>>> call = api.getNotifications(1, 1, true);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Notification>>> call, retrofit2.Response<ApiResponse<List<Notification>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    int count = response.body().getMeta() != null ? response.body().getMeta().getTotal() : (response.body().getData() != null ? response.body().getData().size() : 0);
                    if (count > 0) { badgeView.setText(String.valueOf(count)); badgeView.setVisibility(View.VISIBLE); }
                    else { badgeView.setVisibility(View.GONE); }
                } else {
                    badgeView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Notification>>> call, Throwable t) {
                badgeView.setVisibility(View.GONE);
            }
        });
    }
}


