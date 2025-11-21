package com.example.myrajourney.admin.logs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Notification;

public class AllNotificationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchBar;
    List<Notification> notifications, filteredList;
    NotificationsAdapter adapter;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notifications);

        recyclerView = findViewById(R.id.all_notifications_recycler);
        searchBar = findViewById(R.id.search_bar);
        progress = findViewById(R.id.progress);
        
        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        notifications = new ArrayList<>();

        filteredList = new ArrayList<>(notifications);

        adapter = new NotificationsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadNotifications();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void filter(String query) {
        filteredList.clear();
        for (Notification n : notifications) {
            String hay = (n.getTitle() + " " + (n.getBody() == null ? "" : n.getBody())).toLowerCase();
            if (hay.contains(query.toLowerCase())) {
                filteredList.add(n);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadNotifications() {
        progress.setVisibility(View.VISIBLE);
        ApiService api = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        retrofit2.Call<ApiResponse<List<Notification>>> call = api.getNotifications(1, 20, null);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<Notification>>> call, retrofit2.Response<ApiResponse<List<Notification>>> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    notifications.clear();
                    notifications.addAll(response.body().getData());
                    filter(TextUtils.isEmpty(searchBar.getText()) ? "" : searchBar.getText().toString());
                } else {
                    Toast.makeText(AllNotificationsActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<Notification>>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(AllNotificationsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}






