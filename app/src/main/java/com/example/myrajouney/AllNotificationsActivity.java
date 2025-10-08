package com.example.myrajouney;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AllNotificationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchBar;
    List<String> notifications, filteredList;
    NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notifications);

        recyclerView = findViewById(R.id.all_notifications_recycler);
        searchBar = findViewById(R.id.search_bar);
        
        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        notifications = new ArrayList<>();
        // Show recent 10 notifications
        notifications.add("Patient John Doe's test results are ready");
        notifications.add("New message from Sarah Wilson");
        notifications.add("Appointment reminder: 2:30 PM with Michael Brown");
        notifications.add("3 new patient records added");
        notifications.add("Lab results uploaded by Emily Davis");
        notifications.add("Prescription refill request from Robert Johnson");
        notifications.add("New appointment scheduled with Lisa Anderson");
        notifications.add("Treatment plan updated for David Martinez");
        notifications.add("Follow-up required for Jennifer Taylor");
        notifications.add("Emergency consultation request from William Garcia");

        filteredList = new ArrayList<>(notifications);

        adapter = new NotificationsAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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
        for (String n : notifications) {
            if (n.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(n);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
