package com.example.myrajouney.auth;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myrajouney.core.navigation.NavigationManager;
import com.example.myrajouney.core.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 pager;
    private Button nextButton;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        session = new SessionManager(this);

        pager = findViewById(R.id.viewPager);
        nextButton = findViewById(R.id.btnNext);

        setup();

        nextButton.setOnClickListener(v -> {
            if (pager.getCurrentItem() + 1 < pager.getAdapter().getItemCount()) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            } else {
                finishOnboard();
            }
        });
    }

    private void finishOnboard() {
        session.setOnboardingCompleted(true);
        NavigationManager.goToLogin(this);
        finish();
    }

    private void setup() {
        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem(R.drawable.about1, "Secure Access", "Access safely."));
        items.add(new OnboardingItem(R.drawable.about2, "Track Symptoms", "Manage RA easily."));
        items.add(new OnboardingItem(R.drawable.about3, "Insights", "Analytics at a glance."));

        pager.setAdapter(new OnboardingAdapter(items));

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                nextButton.setText((pos == items.size() - 1) ? "Get Started" : "Next");
            }
        });
    }
}
