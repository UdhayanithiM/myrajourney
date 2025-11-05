package com.example.myrajouney;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myrajouney.api.ApiService;
import com.example.myrajouney.api.models.ApiResponse;
import com.example.myrajouney.api.models.EducationArticle;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EducationHubActivity extends AppCompatActivity {

    private View whatIsRABtn, nutritionBtn, lifestyleBtn, managementBtn;
    private MaterialToolbar toolbar;
    private List<EducationArticle> articles;

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

        // Load education articles from API
        loadEducationArticles();

        // Setup button click listeners for navigation
        setupButtonListeners();
    }

    private void loadEducationArticles() {
        ApiService apiService = com.example.myrajouney.api.ApiClient.getApiService(this);
        Call<ApiResponse<List<EducationArticle>>> call = apiService.getEducationArticles();
        
        call.enqueue(new Callback<ApiResponse<List<EducationArticle>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<EducationArticle>>> call, Response<ApiResponse<List<EducationArticle>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    articles = response.body().getData();
                    // Articles loaded - you can use them for navigation
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<EducationArticle>>> call, Throwable t) {
                // Handle error - app will work with static content as fallback
            }
        });
    }

    private void setupButtonListeners() {
        whatIsRABtn.setOnClickListener(v -> {
            // Try to find article by slug or use static content
            navigateToArticle("what-is-ra", WhatIsRAActivity.class);
        });

        nutritionBtn.setOnClickListener(v -> {
            navigateToArticle("nutrition-tips", NutritionActivity.class);
        });

        lifestyleBtn.setOnClickListener(v -> {
            navigateToArticle("lifestyle", LifestyleActivity.class);
        });

        managementBtn.setOnClickListener(v -> {
            navigateToArticle("managing-symptoms", ManagementActivity.class);
        });
    }

    private void navigateToArticle(String slug, Class<?> fallbackActivity) {
        if (articles != null) {
            for (EducationArticle article : articles) {
                if (article.getSlug().equals(slug)) {
                    // You could create a dynamic article viewer here
                    // For now, use static activities
                    startActivity(new Intent(this, fallbackActivity));
                    return;
                }
            }
        }
        // Fallback to static content
        startActivity(new Intent(this, fallbackActivity));
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