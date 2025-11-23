package com.example.myrajourney.admin.meds;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.patient.medications.MedicationsAdapter;

import java.util.ArrayList;
import java.util.List;

public class AllMedicationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MedicationsAdapter adapter;
    List<Medication> medList;
    EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_medications);

        recyclerView = findViewById(R.id.recycler_view_medications);
        searchBar = findViewById(R.id.search_bar);

        // Dummy Data for Admin View
        medList = new ArrayList<>();
        medList.add(new Medication("Methotrexate", "10mg", "Weekly", "Ongoing", "Tablet", "DMARD", "Active"));
        medList.add(new Medication("Prednisone", "5mg", "Daily", "2 Weeks", "Tablet", "Steroid", "Active"));
        medList.add(new Medication("Hydroxychloroquine", "200mg", "Daily", "Ongoing", "Tablet", "DMARD", "Active"));
        medList.add(new Medication("Ibuprofen", "400mg", "As needed", "N/A", "Tablet", "NSAID", "Active"));

        adapter = new MedicationsAdapter(this, medList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });
    }

    private void filter(String query) {
        List<Medication> filtered = new ArrayList<>();
        for (Medication m : medList) {
            if (m.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(m);
            }
        }
        adapter.filterList(filtered);
    }
}