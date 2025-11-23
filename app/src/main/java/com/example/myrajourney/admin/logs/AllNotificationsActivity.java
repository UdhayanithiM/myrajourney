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

import com.example.myrajourney.R;
import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Notification;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllNotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchBar;
    private ProgressBar progress;

    private final List<Notification> notifications = new ArrayList<>();
    private final List<Notification> filteredList = new ArrayList<>();

    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notifications);

        recyclerView = findViewById(R.id.all_notifications_recycler);
        searchBar = findViewById(R.id.search_bar);
        progress = findViewById(R.id.progress);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        adapter = new NotificationsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadNotifications();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString().trim());
            }
        });
    }

    private void filter(String query) {
        filteredList.clear();

        String q = query.toLowerCase();

        for (Notification n : notifications) {

            String title = n.getTitle() == null ? "" : n.getTitle().toLowerCase();
            String body = n.getBody() == null ? "" : n.getBody().toLowerCase();

            if (title.contains(q) || body.contains(q)) {
                filteredList.add(n);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void loadNotifications() {
        progress.setVisibility(View.VISIBLE);

        ApiService api = ApiClient.getApiService(this);
        Call<ApiResponse<List<Notification>>> call =
                api.getNotifications(1, 20, null);

        call.enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Notification>>> call,
                                   Response<ApiResponse<List<Notification>>> response) {

                progress.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(AllNotificationsActivity.this,
                            "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!response.body().isSuccess()) {
                    Toast.makeText(AllNotificationsActivity.this,
                            "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    return;
                }

                notifications.clear();

                List<Notification> data = response.body().getData();
                if (data != null) notifications.addAll(data);

                filter(TextUtils.isEmpty(searchBar.getText())
                        ? ""
                        : searchBar.getText().toString());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(AllNotificationsActivity.this,
                        "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
