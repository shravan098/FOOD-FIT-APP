package com.example.foodfit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.animation.ValueAnimator;

import java.util.Calendar;

public class Postlogin extends AppCompatActivity {

    private LinearLayout homeSection, profileSection;
    private TextView homeText, profileText, caloriesText, waterCountText;
    private ImageView homeIcon, profileIcon, calendarIcon, addWaterBtn;

    // Meal add (+) icons
    private ImageView addBreakfastBtn, addLunchBtn, addDinnerBtn;

    // 🔹 New: Scan Food button
    private ImageView btnScanFood;

    private int waterCount = 0;
    private int dailyCalorie = 0; // dynamic value from intent
    private int eatenCalorie = 0; // eaten calories, default 0

    // Request codes
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;

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
        calendarIcon = findViewById(R.id.calendarIcon);
        caloriesText = findViewById(R.id.caloriesText);
        waterCountText = findViewById(R.id.waterCountText);
        addWaterBtn = findViewById(R.id.addWaterBtn);

        addBreakfastBtn = findViewById(R.id.addBreakfastBtn);
        addLunchBtn = findViewById(R.id.addLunchBtn);
        addDinnerBtn = findViewById(R.id.addDinnerBtn);

        btnScanFood = findViewById(R.id.btnScanFood);

        dailyCalorie = getIntent().getIntExtra("dailyCalorie", 1340);

        updateCalorieUI();
        updateWaterUI();

        calendarIcon.setOnClickListener(v -> openCalendar());

        addWaterBtn.setOnClickListener(v -> {
            if (waterCount < 9) {
                waterCount++;
                updateWaterUI();
            }
        });

        addBreakfastBtn.setOnClickListener(v -> openMealScreen("Breakfast"));
        addLunchBtn.setOnClickListener(v -> openMealScreen("Lunch"));
        addDinnerBtn.setOnClickListener(v -> openMealScreen("Dinner"));

        btnScanFood.setOnClickListener(v -> showScanOptions());

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

    private void showScanOptions() {
        String[] options = {"📷 Camera", "🖼 Gallery", "❌ Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, REQUEST_CAMERA);

                    } else if (which == 1) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, REQUEST_GALLERY);

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

            } else if (requestCode == REQUEST_GALLERY) {
                Uri selectedImage = data.getData();
                Toast.makeText(this, "🖼 Image selected from Gallery!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Postlogin.this, PreviewActivity.class);
                intent.putExtra("fromCamera", false);
                intent.putExtra("imageUri", selectedImage.toString());
                startActivity(intent);
            }
        }
    }
}