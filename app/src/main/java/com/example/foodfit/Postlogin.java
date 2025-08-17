package com.example.foodfit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.animation.ValueAnimator;

import java.util.Calendar;

public class Postlogin extends AppCompatActivity {

    private LinearLayout homeSection, profileSection;
    private TextView homeText, profileText, caloriesText, waterCountText;
    private ImageView homeIcon, profileIcon, calendarIcon, addWaterBtn;

    // Meal add (+) icons
    private ImageView addBreakfastBtn, addLunchBtn, addDinnerBtn;

    private int waterCount = 0;
    private int dailyCalorie = 1340; // TODO: replace with BMR result from InputActivity
    private int eatenCalorie = 0;    // default eaten

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postlogin);

        // Initialize views
        homeSection = findViewById(R.id.homeSection);
        profileSection = findViewById(R.id.profileSection);
        homeText = findViewById(R.id.homeText);
        profileText = findViewById(R.id.profileText);
        homeIcon = findViewById(R.id.homeIcon);
        profileIcon = findViewById(R.id.profileIcon);
        calendarIcon = findViewById(R.id.calendarIcon); // ImageView, not ImageButton
        caloriesText = findViewById(R.id.caloriesText);
        waterCountText = findViewById(R.id.waterCountText);
        addWaterBtn = findViewById(R.id.addWaterBtn);

        // Meal (+) buttons
        addBreakfastBtn = findViewById(R.id.addBreakfastBtn);
        addLunchBtn = findViewById(R.id.addLunchBtn);
        addDinnerBtn = findViewById(R.id.addDinnerBtn);

        // Default calorie values
        updateCalorieUI();

        // Calendar click → open DatePicker
        calendarIcon.setOnClickListener(v -> openCalendar());

        // Water add button
        addWaterBtn.setOnClickListener(v -> {
            if (waterCount < 9) {
                waterCount++;
                updateWaterUI();
            }
        });

        // Meal add buttons → open new screen
        addBreakfastBtn.setOnClickListener(v -> openMealScreen("Breakfast"));
        addLunchBtn.setOnClickListener(v -> openMealScreen("Lunch"));
        addDinnerBtn.setOnClickListener(v -> openMealScreen("Dinner"));

        // Bottom navigation
        homeSection.setOnClickListener(v -> switchTab(true));
        profileSection.setOnClickListener(v -> switchTab(false));
    }

    private void updateCalorieUI() {
        caloriesText.setText(eatenCalorie + " / " + dailyCalorie);
    }

    private void updateWaterUI() {
        waterCountText.setText(waterCount + "/9");
    }

    private void openCalendar() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) ->
                        calendarIcon.setContentDescription("Selected: " + dayOfMonth + "/" + (month1 + 1) + "/" + year1),
                year, month, day);
        datePickerDialog.show();
    }

    private void openMealScreen(String mealType) {
        Intent intent = new Intent(Postlogin.this, FoodSearchActivity.class);
        intent.putExtra("mealType", mealType);
        startActivity(intent);
    }

    private void switchTab(boolean isHome) {
        LinearLayout.LayoutParams homeParams = (LinearLayout.LayoutParams) homeSection.getLayoutParams();
        LinearLayout.LayoutParams profileParams = (LinearLayout.LayoutParams) profileSection.getLayoutParams();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            if (isHome) {
                homeParams.weight = 7;
                profileParams.weight = 3;

                homeText.setTextColor(Color.WHITE);
                homeIcon.setColorFilter(Color.WHITE);
                profileText.setTextColor(Color.parseColor("#80FFFFFF"));
                profileIcon.setColorFilter(Color.parseColor("#80FFFFFF"));

            } else {
                homeParams.weight = 3;
                profileParams.weight = 7;

                profileText.setTextColor(Color.WHITE);
                profileIcon.setColorFilter(Color.WHITE);
                homeText.setTextColor(Color.parseColor("#80FFFFFF"));
                homeIcon.setColorFilter(Color.parseColor("#80FFFFFF"));
            }

            homeSection.setLayoutParams(homeParams);
            profileSection.setLayoutParams(profileParams);
        });
        animator.start();
    }
}
