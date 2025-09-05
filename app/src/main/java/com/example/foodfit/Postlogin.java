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

public class Postlogin extends AppCompatActivity {

    private LinearLayout homeSection, profileSection;
    private TextView homeText, profileText, caloriesText, waterCountText;
    private ImageView homeIcon, profileIcon, calendarIcon, addWaterBtn;
    private ImageView addBreakfastBtn, addLunchBtn, addDinnerBtn;
    private TextView breakfastCaloriesText, lunchCaloriesText, dinnerCaloriesText;
    private TextView tvBreakfastTarget, tvLunchTarget, tvDinnerTarget;

    private int waterCount = 0, waterTarget = 9, dailyCalorie = 0, eatenCalorie = 0;
    private int breakfastTarget, lunchTarget, dinnerTarget;
    private int breakfastConsumed = 0, lunchConsumed = 0, dinnerConsumed = 0;

    private String userId;
    private FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postlogin);

        userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) { Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show(); finish(); return; }

        firestore = FirebaseFirestore.getInstance();
        initViews();
        loadUserProfileFromFirestore();

        calendarIcon.setOnClickListener(v -> openCalendar());
        addWaterBtn.setOnClickListener(v -> { if (waterCount < waterTarget) { waterCount++; updateWaterUI(); } });

        addBreakfastBtn.setOnClickListener(v -> startMealActivity("breakfast", BreakfastMealActivity.class));
        addLunchBtn.setOnClickListener(v -> startMealActivity("lunch", Lunchmealactivity.class));
        addDinnerBtn.setOnClickListener(v -> startMealActivity("dinner", DinnerMealActivity.class));

        homeSection.setOnClickListener(v -> switchTab(true));
        profileSection.setOnClickListener(v -> switchTab(false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Highlight home tab
        homeText.setTextColor(Color.WHITE);
        homeIcon.setColorFilter(Color.WHITE);
        profileText.setTextColor(Color.parseColor("#80FFFFFF"));
        profileIcon.setColorFilter(Color.parseColor("#80FFFFFF"));

        LinearLayout.LayoutParams homeParams = (LinearLayout.LayoutParams) homeSection.getLayoutParams();
        LinearLayout.LayoutParams profileParams = (LinearLayout.LayoutParams) profileSection.getLayoutParams();
        homeParams.weight = 7;
        profileParams.weight = 3;
        homeSection.setLayoutParams(homeParams);
        profileSection.setLayoutParams(profileParams);
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

    private void startMealActivity(String type, Class<?> cls) {
        Intent intent = new Intent(Postlogin.this, cls);
        intent.putExtra("mealType", type);
        startActivity(intent);
    }

    private void calculateWaterTarget(int weightKg) { waterTarget = (int) Math.round(weightKg * 35 / 250.0); }

    private void allocateMealCalories() {
        breakfastTarget = (int)(dailyCalorie*0.3);
        lunchTarget = (int)(dailyCalorie*0.4);
        dinnerTarget = dailyCalorie-(breakfastTarget+ lunchTarget);
        updateMealUI();
    }

    private void updateCalorieUI() { caloriesText.setText(eatenCalorie + " / " + dailyCalorie); }

    private void updateMealUI() {
        updateSingleMealUI(tvBreakfastTarget, breakfastCaloriesText, breakfastConsumed, breakfastTarget);
        updateSingleMealUI(tvLunchTarget, lunchCaloriesText, lunchConsumed, lunchTarget);
        updateSingleMealUI(tvDinnerTarget, dinnerCaloriesText, dinnerConsumed, dinnerTarget);
        eatenCalorie = breakfastConsumed + lunchConsumed + dinnerConsumed;
        updateCalorieUI();
    }

    private void updateSingleMealUI(TextView targetTv, TextView consumedTv, int consumed, int target) {
        targetTv.setText("Target: " + target + " cal");
        consumedTv.setText(consumed + " / " + target + " kcal");
        if (consumed == target) consumedTv.setTextColor(Color.GREEN);
        else if (consumed > target + 50) consumedTv.setTextColor(Color.RED);
        else consumedTv.setTextColor(Color.WHITE);
    }

    private void loadUserProfileFromFirestore() {
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if(snapshot.exists()) {
                        Long cal = snapshot.getLong("dailyCalorie");
                        if(cal != null) dailyCalorie = cal.intValue();

                        int weight = 70;
                        String weightStr = snapshot.getString("weight");
                        if(weightStr != null && !weightStr.isEmpty()) try { weight = Integer.parseInt(weightStr); } catch(Exception e){}

                        calculateWaterTarget(weight);
                        allocateMealCalories();
                        updateWaterUI();
                        loadMealProgressFromFirestore();
                    }
                });
    }

    private void loadMealProgressFromFirestore() {
        String[] meals = {"Breakfast","Lunch","Dinner"};
        for(String mealType: meals){
            firestore.collection("users").document(userId).collection("meals")
                    .whereEqualTo("mealType", mealType)
                    .addSnapshotListener((snapshots, e) -> {
                        if(e != null) return;
                        int total = 0;
                        if(snapshots != null){
                            for(DocumentSnapshot doc: snapshots){
                                String calStr = doc.getString("calories");
                                if(calStr != null && !calStr.isEmpty()) total += (int)Double.parseDouble(calStr);
                            }
                        }
                        switch(mealType){
                            case "Breakfast": breakfastConsumed = total; break;
                            case "Lunch": lunchConsumed = total; break;
                            case "Dinner": dinnerConsumed = total; break;
                        }
                        updateMealUI();
                    });
        }
    }

    private void updateWaterUI() { waterCountText.setText(waterCount + "/" + waterTarget); }

    private void openCalendar() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) -> calendarIcon.setContentDescription("Selected: "+day+"/"+(month+1)+"/"+year),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void switchTab(boolean isHome){
        LinearLayout.LayoutParams homeParams = (LinearLayout.LayoutParams) homeSection.getLayoutParams();
        LinearLayout.LayoutParams profileParams = (LinearLayout.LayoutParams) profileSection.getLayoutParams();

        ValueAnimator animator = ValueAnimator.ofFloat(0f,1f);
        animator.setDuration(400);
        animator.setInterpolator(new android.view.animation.OvershootInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            if(isHome){
                homeParams.weight = 3 + 4*fraction;
                profileParams.weight = 7 - 4*fraction;
            } else{
                homeParams.weight = 7 - 4*fraction;
                profileParams.weight = 3 + 4*fraction;
            }
            homeSection.setLayoutParams(homeParams);
            profileSection.setLayoutParams(profileParams);

            homeText.setTextColor(isHome ? blendColors(Color.parseColor("#80FFFFFF"), Color.WHITE, fraction)
                    : blendColors(Color.WHITE, Color.parseColor("#80FFFFFF"), fraction));
            profileText.setTextColor(isHome ? blendColors(Color.WHITE, Color.parseColor("#80FFFFFF"), fraction)
                    : blendColors(Color.parseColor("#80FFFFFF"), Color.WHITE, fraction));
            homeIcon.setColorFilter(isHome ? blendColors(Color.parseColor("#80FFFFFF"), Color.WHITE, fraction)
                    : blendColors(Color.WHITE, Color.parseColor("#80FFFFFF"), fraction));
            profileIcon.setColorFilter(isHome ? blendColors(Color.WHITE, Color.parseColor("#80FFFFFF"), fraction)
                    : blendColors(Color.parseColor("#80FFFFFF"), Color.WHITE, fraction));
        });
        animator.start();

        if(!isHome) startActivity(new Intent(Postlogin.this, ProfileActivity.class));
    }

    private int blendColors(int from,int to,float fraction){
        int r = (int)((Color.red(to)-Color.red(from))*fraction + Color.red(from));
        int g = (int)((Color.green(to)-Color.green(from))*fraction + Color.green(from));
        int b = (int)((Color.blue(to)-Color.blue(from))*fraction + Color.blue(from));
        return Color.rgb(r,g,b);
    }
}
