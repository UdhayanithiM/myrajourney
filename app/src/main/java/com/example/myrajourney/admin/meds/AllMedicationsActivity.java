package com.example.myrajourney.admin.meds;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.Medication;
// Importing the adapter from the patient package where it resides
import com.example.myrajourney.patient.medications.MedicationsAdapter;
// ---------------------

import java.util.ArrayList;
import java.util.List;

public class AllMedicationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MedicationsAdapter adapter;
    List<Medication> medList;
    EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme (Good practice to keep consistent with other Admin screens)
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_medications);

        recyclerView = findViewById(R.id.recycler_view_medications);
        searchBar = findViewById(R.id.search_bar);

        // Initialize medications with dummy data
        // Note: Ensure your Medication model constructor matches these arguments
        medList = new ArrayList<>();
        medList.add(new Medication("Paracetamol", "500mg", "Twice a day", "5 days", "Tablet", "Painkiller", "Ongoing"));
        medList.add(new Medication("Ibuprofen", "400mg", "Once a day", "7 days", "Tablet", "Painkiller", "Completed"));
        medList.add(new Medication("Amoxicillin", "250mg", "Thrice a day", "10 days", "Capsule", "Antibiotic", "Ongoing"));

        // Setup adapter
        adapter = new MedicationsAdapter(this, medList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Search bar filter
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });
    }

    private void filter(String query) {
        List<Medication> filtered = new ArrayList<>();
        for (Medication m : medList) {
            // Check if name or type matches query (case-insensitive)
            if (m.getName().toLowerCase().contains(query.toLowerCase()) ||
                    m.getType().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(m);
            }
        }
        adapter.filterList(filtered);
    }
}