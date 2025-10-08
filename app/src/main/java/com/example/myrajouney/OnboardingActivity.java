package com.example.myrajouney;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private Button btnNext;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Force full screen mode
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        dotsLayout = findViewById(R.id.dotsLayout);

        setupOnboardingItems();
        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);

                if (position == onboardingAdapter.getItemCount() - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                // Move to next onboarding page
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                // ✅ Go to LoginActivity after last screen
                Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> items = new ArrayList<>();

        items.add(new OnboardingItem(R.drawable.about1,
                "Secure Access & User Roles",
                "Access My RA Journey securely whether you’re a patient or a doctor..."));

        items.add(new OnboardingItem(R.drawable.about2,
                "Track and Manage RA With Ease",
                "Doctors and patients both enjoy a powerful dashboard..."));

        items.add(new OnboardingItem(R.drawable.about3,
                "Insights, Analytics & Education",
                "Track your journey with analytics, see trends in pain..."));

        onboardingAdapter = new OnboardingAdapter(items);
        viewPager.setAdapter(onboardingAdapter);
    }

    private void setupDots(int position) {
        dots = new TextView[onboardingAdapter.getItemCount()];
        dotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText("•");
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(
                    i == position ? android.R.color.black : android.R.color.darker_gray
            ));
            dotsLayout.addView(dots[i]);
        }
    }
}
