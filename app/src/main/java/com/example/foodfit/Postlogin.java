package com.example.foodfit;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ValueAnimator;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Postlogin extends AppCompatActivity {

    private LinearLayout homeSection, profileSection;
    private TextView homeText, profileText, caloriesText, waterCountText;
    private ImageView homeIcon, profileIcon, calendarIcon, addWaterBtn;

    // Meal add (+) icons
    private ImageView addBreakfastBtn, addLunchBtn, addDinnerBtn;

    // Meal calorie TextViews
    private TextView breakfastCaloriesText, lunchCaloriesText, dinnerCaloriesText;

    private int waterCount = 0;
    private int waterTarget = 9; // default
    private int dailyCalorie = 0;
    private int eatenCalorie = 0;

    // Meal tracking
    private int breakfastTarget, lunchTarget, dinnerTarget;
    private int breakfastConsumed = 0, lunchConsumed = 0, dinnerConsumed = 0;
    private TextView tvBreakfastTarget, tvLunchTarget, tvDinnerTarget;

    // Firebase
    private String userId;
    private FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postlogin);

        userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore = FirebaseFirestore.getInstance();
        initViews();

        // Load profile and meal data
        loadUserProfileFromFirestore();

        calendarIcon.setOnClickListener(v -> openCalendar());

        addWaterBtn.setOnClickListener(v -> {
            if (waterCount < waterTarget) {
                waterCount++;
                updateWaterUI();
            }
        });

        addBreakfastBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Postlogin.this, BreakfastMealActivity.class);
            intent.putExtra("mealType", "breakfast");
            startActivity(intent);
        });

        addLunchBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Postlogin.this, Lunchmealactivity.class);
            intent.putExtra("mealType", "lunch");
            startActivity(intent);
        });

        addDinnerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Postlogin.this, DinnerMealActivity.class);
            intent.putExtra("mealType", "dinner");
            startActivity(intent);
        });

        homeSection.setOnClickListener(v -> switchTab(true));
        profileSection.setOnClickListener(v -> switchTab(false));
    }

    private void initViews() {
        homeSection = findViewById(R.id.homeSection);
        profileSection = findViewById(R.id.profileSection);
        homeText = findViewById(R.id.homeText);
        profileText = findViewById(R.id.profileText);
        homeIcon = findViewById(R.id.homeIcon);
        profileIcon = findViewById(R.id.profileIcon);
        calendarIcon = findViewById(R.id.calendarIcon);
        caloriesText = findViewById(R.id.caloriesText);
        waterCountText = findViewById(R.id.waterCountText);
        addWaterBtn = findViewById(R.id.addWaterBtn);

        addBreakfastBtn = findViewById(R.id.addBreakfastBtn);
        addLunchBtn = findViewById(R.id.addLunchBtn);
        addDinnerBtn = findViewById(R.id.addDinnerBtn);
        tvBreakfastTarget = findViewById(R.id.tvBreakfastTarget);
        tvLunchTarget = findViewById(R.id.tvLunchTarget);
        tvDinnerTarget = findViewById(R.id.tvDinnerTarget);

        breakfastCaloriesText = findViewById(R.id.breakfastCaloriesText);
        lunchCaloriesText = findViewById(R.id.lunchCaloriesText);
        dinnerCaloriesText = findViewById(R.id.dinnerCaloriesText);
    }

    private void calculateWaterTarget(int weightKg) {
        double waterMl = weightKg * 35;
        waterTarget = (int) Math.round(waterMl / 250.0);
    }

    private void allocateMealCalories() {
        breakfastTarget = (int) (dailyCalorie * 0.3);
        lunchTarget = (int) (dailyCalorie * 0.4);
        dinnerTarget = dailyCalorie - (breakfastTarget + lunchTarget);

        updateMealUI();
    }

    private void updateCalorieUI() {
        caloriesText.setText(eatenCalorie + " / " + dailyCalorie); // Eaten / Total
    }

    private void updateMealUI() {
        // Breakfast
        updateSingleMealUI(tvBreakfastTarget, breakfastCaloriesText, breakfastConsumed, breakfastTarget);

        // Lunch
        updateSingleMealUI(tvLunchTarget, lunchCaloriesText, lunchConsumed, lunchTarget);

        // Dinner
        updateSingleMealUI(tvDinnerTarget, dinnerCaloriesText, dinnerConsumed, dinnerTarget);

        // Update total calories
        eatenCalorie = breakfastConsumed + lunchConsumed + dinnerConsumed;
        updateCalorieUI();
    }

    private void updateSingleMealUI(TextView targetTv, TextView consumedTv, int consumed, int target) {
        targetTv.setText("Target: " + target + " cal");
        consumedTv.setText(consumed + " / " + target + " kcal");

        if (consumed == target) {
            consumedTv.setTextColor(Color.GREEN);
            consumedTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else if (consumed > target + 50) {
            consumedTv.setTextColor(Color.RED);
            consumedTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.refs, 0);
        } else {
            consumedTv.setTextColor(Color.WHITE);
            consumedTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    private void loadUserProfileFromFirestore() {
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Long cal = snapshot.getLong("dailyCalorie");
                        if (cal != null) dailyCalorie = cal.intValue();

                        String weightStr = snapshot.getString("weight");
                        int weightKg = 70; // default
                        if (weightStr != null && !weightStr.isEmpty()) {
                            try { weightKg = Integer.parseInt(weightStr); } catch (NumberFormatException e) { e.printStackTrace(); }
                        }

                        calculateWaterTarget(weightKg);
                        allocateMealCalories();
                        updateWaterUI();

                        // Load meal progress realtime
                        loadMealProgressFromFirestore();

                        Toast.makeText(this, "✅ Profile loaded", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "⚠️ Profile not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "❌ Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadMealProgressFromFirestore() {
        String[] meals = {"Breakfast", "Lunch", "Dinner"};
        for (String mealType : meals) {
            firestore.collection("users").document(userId).collection("meals")
                    .whereEqualTo("mealType", mealType)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Toast.makeText(Postlogin.this, "❌ " + mealType + " listen failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int totalCalories = 0;
                        if (queryDocumentSnapshots != null) {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                String calStr = doc.getString("calories");
                                if (calStr != null && !calStr.isEmpty()) {
                                    try { totalCalories += (int) Double.parseDouble(calStr); } catch (NumberFormatException ex) { ex.printStackTrace(); }
                                }
                            }
                        }

                        switch (mealType) {
                            case "Breakfast": breakfastConsumed = totalCalories; break;
                            case "Lunch": lunchConsumed = totalCalories; break;
                            case "Dinner": dinnerConsumed = totalCalories; break;
                        }

                        // Update UI including total calories
                        updateMealUI();
                    });
        }
    }

    private void updateWaterUI() {
        waterCountText.setText(waterCount + "/" + waterTarget);
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
