package com.example.myrajouney;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class EducationHubActivity extends AppCompatActivity {

    private View whatIsRABtn, nutritionBtn, lifestyleBtn, managementBtn;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_hub);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        whatIsRABtn = findViewById(R.id.whatIsRABtn);
        nutritionBtn = findViewById(R.id.nutritionBtn);
        lifestyleBtn = findViewById(R.id.lifestyleBtn);
        managementBtn = findViewById(R.id.managementBtn);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Education Hub");
        }

        // Setup button click listeners for navigation
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        whatIsRABtn.setOnClickListener(v -> {
            startActivity(new Intent(this, WhatIsRAActivity.class));
        });

        nutritionBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, NutritionActivity.class));
        });

        lifestyleBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LifestyleActivity.class));
        });

        managementBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, ManagementActivity.class));
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}