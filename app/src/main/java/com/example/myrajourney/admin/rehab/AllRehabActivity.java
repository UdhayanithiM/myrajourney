package com.example.myrajourney.admin.rehab;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AllRehabActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RehabAdapter adapter;
    List<Rehab> rehabList;
    EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_rehab);

        recyclerView = findViewById(R.id.all_rehab_recycler);
        searchBar = findViewById(R.id.search_bar);

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

        adapter = new RehabAdapter(this, rehabList);
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
    }

    private void filter(String text) {
        List<Rehab> temp = new ArrayList<>();
        for (Rehab rehab : rehabList) {
            if (rehab.getName().toLowerCase().contains(text.toLowerCase()) ||
                    rehab.getDescription().toLowerCase().contains(text.toLowerCase())) {
                temp.add(rehab);
            }
        }
        adapter.filterList(temp);
    }
}






