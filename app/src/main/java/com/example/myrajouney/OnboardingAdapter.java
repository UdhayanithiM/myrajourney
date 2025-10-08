package com.example.myrajouney;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OnboardingViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_onboarding, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.setOnboardingData(onboardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageOnboarding;
        TextView titleOnboarding, descOnboarding;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageOnboarding = itemView.findViewById(R.id.imageOnboarding);
            titleOnboarding = itemView.findViewById(R.id.titleOnboarding);
            descOnboarding = itemView.findViewById(R.id.descOnboarding);
        }

        void setOnboardingData(OnboardingItem item) {
            imageOnboarding.setImageResource(item.image);
            titleOnboarding.setText(item.title);
            descOnboarding.setText(item.description);
        }
    }
}
