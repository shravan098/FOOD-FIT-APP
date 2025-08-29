package com.example.foodfit;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ValueAnimator;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.PickVisualMediaRequest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
// Add impor
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class Postlogin extends AppCompatActivity {
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private LinearLayout homeSection, profileSection;
    private TextView homeText, profileText, caloriesText, waterCountText;
    private ImageView homeIcon, profileIcon, calendarIcon, addWaterBtn;
    private ActivityResultLauncher<PickVisualMediaRequest> galleryPickerLauncher;

    // Meal add (+) icons
    private ImageView addBreakfastBtn, addLunchBtn, addDinnerBtn;

    // Meal calorie TextViews
    private TextView breakfastCaloriesText, lunchCaloriesText, dinnerCaloriesText;

    // 🔹 New: Scan Food button
    private ImageView btnScanFood;

    private int waterCount = 0;
    private int waterTarget = 9; // default
    private int dailyCalorie = 0;
    private int eatenCalorie = 0;

    // Meal tracking
    private int breakfastTarget, lunchTarget, dinnerTarget;
    private int breakfastConsumed = 0, lunchConsumed = 0, dinnerConsumed = 0;
    private TextView tvBreakfastTarget, tvLunchTarget, tvDinnerTarget;

    // Firebase
    private DatabaseReference dbRef;
    private String userId;

    // Request codes
    private static final int REQUEST_CAMERA = 100;

    private FirebaseFirestore firestore;
    private void launchCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

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

        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("dailyProgress");
        firestore = FirebaseFirestore.getInstance();

        initViews();

        // ✅ sirf ye call karna hai, baaki sab iske andar hoga
        loadUserProfileFromFirestore();

        calendarIcon.setOnClickListener(v -> openCalendar());
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchCameraIntent();
                    } else {
                        Toast.makeText(this, "⚠️ Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        galleryPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        Toast.makeText(this, "🖼 Image selected from Gallery!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Postlogin.this, PreviewActivity.class);
                        intent.putExtra("fromCamera", false);
                        intent.putExtra("imageUri", uri.toString());
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "⚠️ No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        addWaterBtn.setOnClickListener(v -> {
            if (waterCount < waterTarget) {
                waterCount++;
                updateWaterUI();
                saveMealProgressToFirebase();
            }
        });

        addBreakfastBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Postlogin.this, FoodSearchActivity.class);
            intent.putExtra("mealType", "breakfast");
            startActivity(intent);
        });

        addLunchBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Postlogin.this, FoodSearchActivity.class);
            intent.putExtra("mealType", "lunch");
            startActivity(intent);
        });

        addDinnerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Postlogin.this, FoodSearchActivity.class);
            intent.putExtra("mealType", "dinner");
            startActivity(intent);
        });

        btnScanFood.setOnClickListener(v -> showScanOptions());

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

        btnScanFood = findViewById(R.id.btnScanFood);
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
        caloriesText.setText(eatenCalorie + " / " + dailyCalorie);
    }

    private void updateMealUI() {
        tvBreakfastTarget.setText("Target: " + breakfastTarget + " cal");
        breakfastCaloriesText.setText(breakfastConsumed + " / " + breakfastTarget + " kcal");

        tvLunchTarget.setText("Target: " + lunchTarget + " cal");
        lunchCaloriesText.setText(lunchConsumed + " / " + lunchTarget + " kcal");

        tvDinnerTarget.setText("Target: " + dinnerTarget + " cal");
        dinnerCaloriesText.setText(dinnerConsumed + " / " + dinnerTarget + " kcal");

        caloriesText.setText(eatenCalorie + " / " + dailyCalorie);
    }

    private void addFoodToMeal(String mealType, int foodCalories) {
        switch (mealType) {
            case "breakfast":
                breakfastConsumed += foodCalories;
                break;
            case "lunch":
                lunchConsumed += foodCalories;
                break;
            case "dinner":
                dinnerConsumed += foodCalories;
                break;
        }
        eatenCalorie += foodCalories;
        updateCalorieUI();
        updateMealUI();
        saveMealProgressToFirebase();
    }
    private void loadUserProfileFromFirestore() {
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // dailyCalorie
                        Long cal = snapshot.getLong("dailyCalorie");
                        if (cal != null) {
                            dailyCalorie = cal.intValue();
                        }

                        // weight
                        String weightStr = snapshot.getString("weight");
                        int weightKg = 70; // default
                        if (weightStr != null && !weightStr.isEmpty()) {
                            try {
                                weightKg = Integer.parseInt(weightStr);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }

                        // ✅ Update targets & UI
                        calculateWaterTarget(weightKg);
                        allocateMealCalories();
                        updateCalorieUI();
                        updateWaterUI();

                        // ✅ Load daily progress from Realtime DB
                        loadMealProgressFromFirebase();

                        Toast.makeText(this, "✅ Profile loaded", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "⚠️ Profile not found in Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveMealProgressToFirebase() {
        String today = getTodayDate();

        DatabaseReference ref = dbRef.child(today);

        ref.child("breakfastConsumed").setValue(breakfastConsumed);
        ref.child("breakfastTarget").setValue(breakfastTarget);

        ref.child("lunchConsumed").setValue(lunchConsumed);
        ref.child("lunchTarget").setValue(lunchTarget);

        ref.child("dinnerConsumed").setValue(dinnerConsumed);
        ref.child("dinnerTarget").setValue(dinnerTarget);

        ref.child("waterCount").setValue(waterCount);
        ref.child("waterTarget").setValue(waterTarget);
        ref.child("eatenCalorie").setValue(eatenCalorie);
        ref.child("dailyCalorie").setValue(dailyCalorie);
    }

    private void loadMealProgressFromFirebase() {
        String today = getTodayDate();
        DatabaseReference ref = dbRef.child(today);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    breakfastConsumed = snapshot.child("breakfastConsumed").getValue(Integer.class) != null ?
                            snapshot.child("breakfastConsumed").getValue(Integer.class) : 0;
                    lunchConsumed = snapshot.child("lunchConsumed").getValue(Integer.class) != null ?
                            snapshot.child("lunchConsumed").getValue(Integer.class) : 0;
                    dinnerConsumed = snapshot.child("dinnerConsumed").getValue(Integer.class) != null ?
                            snapshot.child("dinnerConsumed").getValue(Integer.class) : 0;
                    waterCount = snapshot.child("waterCount").getValue(Integer.class) != null ?
                            snapshot.child("waterCount").getValue(Integer.class) : 0;
                    eatenCalorie = snapshot.child("eatenCalorie").getValue(Integer.class) != null ?
                            snapshot.child("eatenCalorie").getValue(Integer.class) : 0;
                    waterTarget = snapshot.child("waterTarget").getValue(Integer.class) != null ?
                            snapshot.child("waterTarget").getValue(Integer.class) : waterTarget;

                    updateMealUI();
                    updateCalorieUI();
                    updateWaterUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Postlogin.this, "❌ Load failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
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

    private void showScanOptions() {
        String[] options = {"📷 Camera", "🖼 Gallery", "❌ Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            launchCameraIntent();
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }

                    } else if (which == 1) {
                        galleryPickerLauncher.launch(
                                new PickVisualMediaRequest.Builder()
                                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                        .build()
                        );

                    } else {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Toast.makeText(this, "📸 Camera photo captured!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Postlogin.this, PreviewActivity.class);
                intent.putExtra("fromCamera", true);
                intent.putExtra("cameraBitmap", photo);
                startActivity(intent);

            }
        }
    }
}
