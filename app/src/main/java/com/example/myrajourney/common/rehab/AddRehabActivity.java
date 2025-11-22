package com.example.myrajourney.common.rehab;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- ADDED IMPORTS ---
import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;
import com.example.myrajourney.data.model.Rehab;
// ---------------------

import java.util.ArrayList;
import java.util.List;

public class AddRehabActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AddRehabAdapter adapter;
    private List<Rehab> rehabList;
    private EditText searchBar;
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rehab);

        recyclerView = findViewById(R.id.add_rehab_recycler);
        searchBar = findViewById(R.id.search_bar);
        doneButton = findViewById(R.id.done_button);

        rehabList = new ArrayList<>();
        rehabList.add(new Rehab("Fist Squeeze", "Squeeze soft ball", "10 reps", "Daily",
                "https://www.youtube.com/watch?v=5qny4scQqHc",
                "https://img.youtube.com/vi/5qny4scQqHc/0.jpg"));
        rehabList.add(new Rehab("Finger Spread", "Spread fingers apart", "5 reps", "Daily",
                "https://www.youtube.com/watch?v=DRr4qzxCSqY",
                "https://img.youtube.com/vi/DRr4qzxCSqY/0.jpg"));
        rehabList.add(new Rehab("Wrist Flex", "Flex wrist upward", "10 reps", "Daily",
                "https://www.youtube.com/watch?v=NXbtJ6qCdbs",
                "https://img.youtube.com/vi/NXbtJ6qCdbs/0.jpg"));

        adapter = new AddRehabAdapter(this, rehabList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });

        doneButton.setOnClickListener(v -> {
            ArrayList<Rehab> selected = new ArrayList<>();
            for (Rehab r : rehabList) {
                if (r.isSelected()) selected.add(r);
            }

            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("selected_rehab", selected);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void filter(String text) {
        List<Rehab> temp = new ArrayList<>();
        for (Rehab rehab : rehabList) {
            if (rehab.getName().toLowerCase().contains(text.toLowerCase())) {
                temp.add(rehab);
            }
        }
        adapter.filterList(temp);
    }
}