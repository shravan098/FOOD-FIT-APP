package com.example.foodfit;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ValueAnimator;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName;
    private Button btnEditProfile, btnSignOut, btnShareApp, btnContactUs, btnRateApp;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String userId;

    private LinearLayout homeSection, profileSection;
    private TextView homeText, profileText;
    private ImageView homeIcon, profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        // Profile UI
        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnShareApp = findViewById(R.id.btnShareApp);
        btnContactUs = findViewById(R.id.btnContactUs);
        btnRateApp = findViewById(R.id.btnRateApp);

        // Bottom navigation
        homeSection = findViewById(R.id.homeSection);
        profileSection = findViewById(R.id.profileSection);
        homeText = findViewById(R.id.homeText);
        profileText = findViewById(R.id.profileText);
        homeIcon = findViewById(R.id.homeIcon);
        profileIcon = findViewById(R.id.profileIcon);

        loadUserProfile();

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        profileName.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        btnSignOut.setOnClickListener(v -> signOutUser());
        btnShareApp.setOnClickListener(v -> shareApp());
        btnContactUs.setOnClickListener(v -> contactUs());
        btnRateApp.setOnClickListener(v -> showRatingDialog());

        homeSection.setOnClickListener(v -> switchTab(true));
        profileSection.setOnClickListener(v -> switchTab(false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Highlight profile tab
        profileText.setTextColor(Color.WHITE);
        profileIcon.setColorFilter(Color.WHITE);
        homeText.setTextColor(Color.parseColor("#80FFFFFF"));
        homeIcon.setColorFilter(Color.parseColor("#80FFFFFF"));

        LinearLayout.LayoutParams homeParams = (LinearLayout.LayoutParams) homeSection.getLayoutParams();
        LinearLayout.LayoutParams profileParams = (LinearLayout.LayoutParams) profileSection.getLayoutParams();
        homeParams.weight = 3;
        profileParams.weight = 7;
        homeSection.setLayoutParams(homeParams);
        profileSection.setLayoutParams(profileParams);
    }

    private void loadUserProfile() {
        if (userId == null) return;

        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String username = snapshot.getString("username");
                        String gender = snapshot.getString("gender");
                        profileName.setText(username != null ? username : "User Name");

                        int placeholderRes;
                        if ("male".equalsIgnoreCase(gender)) placeholderRes = R.drawable.ic_profile_male;
                        else if ("female".equalsIgnoreCase(gender)) placeholderRes = R.drawable.ic_profile_female;
                        else placeholderRes = R.drawable.ic_profile_placeholder_circle;

                        Glide.with(ProfileActivity.this)
                                .load(placeholderRes)
                                .circleCrop()
                                .into(profileImage);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    profileName.setText("User Name");
                    Glide.with(this)
                            .load(R.drawable.ic_profile_placeholder_circle)
                            .circleCrop()
                            .into(profileImage);
                });
    }

    private void switchTab(boolean goHome) {
        LinearLayout.LayoutParams homeParams = (LinearLayout.LayoutParams) homeSection.getLayoutParams();
        LinearLayout.LayoutParams profileParams = (LinearLayout.LayoutParams) profileSection.getLayoutParams();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(400);
        animator.setInterpolator(new android.view.animation.OvershootInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();

            if (goHome) {
                homeParams.weight = 3 + 4 * fraction;
                profileParams.weight = 7 - 4 * fraction;
            } else {
                homeParams.weight = 7 - 4 * fraction;
                profileParams.weight = 3 + 4 * fraction;
            }
            homeSection.setLayoutParams(homeParams);
            profileSection.setLayoutParams(profileParams);

            homeText.setTextColor(blendColors(Color.parseColor("#80FFFFFF"), Color.WHITE, goHome ? fraction : 1 - fraction));
            profileText.setTextColor(blendColors(Color.parseColor("#80FFFFFF"), Color.WHITE, goHome ? 1 - fraction : fraction));
            homeIcon.setColorFilter(blendColors(Color.parseColor("#80FFFFFF"), Color.WHITE, goHome ? fraction : 1 - fraction));
            profileIcon.setColorFilter(blendColors(Color.parseColor("#80FFFFFF"), Color.WHITE, goHome ? 1 - fraction : fraction));
        });
        animator.start();

        if (goHome) {
            startActivity(new Intent(this, Postlogin.class));
            finish();
        }
    }

    private int blendColors(int from, int to, float fraction) {
        int r = (int) ((Color.red(to) - Color.red(from)) * fraction + Color.red(from));
        int g = (int) ((Color.green(to) - Color.green(from)) * fraction + Color.green(from));
        int b = (int) ((Color.blue(to) - Color.blue(from)) * fraction + Color.blue(from));
        return Color.rgb(r, g, b);
    }

    private void signOutUser() {
        auth.signOut();
        Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, PlanSelectionActivity.class));
        finish();
    }

    private void shareApp() {
        try {
            // Get current APK path
            android.content.pm.ApplicationInfo app = getApplicationContext().getApplicationInfo();
            String filePath = app.sourceDir;
            java.io.File originalApk = new java.io.File(filePath);

            // Copy APK to cache for sharing
            java.io.File cacheFile = new java.io.File(getCacheDir(), "FoodFit.apk");
            try (java.io.InputStream in = new java.io.FileInputStream(originalApk);
                 java.io.OutputStream out = new java.io.FileOutputStream(cacheFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }

            // Get URI from FileProvider
            android.net.Uri apkUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    cacheFile
            );

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.android.package-archive");
            shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share App via"));

        } catch (Exception e) {
            Toast.makeText(this, "Unable to share app: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private void contactUs() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(android.net.Uri.parse("mailto:shravanjadhav041@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact from FoodFit App");
        try { startActivity(Intent.createChooser(emailIntent, "Send mail using...")); }
        catch (Exception e) { Toast.makeText(this, "No email client found.", Toast.LENGTH_SHORT).show(); }
    }

    private void showRatingDialog() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.popup_rating);
        dialog.setCancelable(true);

        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        ratingBar.setIsIndicator(false);
        ratingBar.setStepSize(1f);
        ratingBar.setNumStars(5);

        final float[] userRating = {0};
        ratingBar.setOnRatingBarChangeListener((rb, rating, fromUser) -> { if (fromUser) userRating[0] = rating; });

        Button btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnDismissListener(d -> {
            if (userId != null && userRating[0] > 0) {
                HashMap<String, Object> data = new HashMap<>();
                data.put("rating", userRating[0]);
                data.put("ratingByName", profileName.getText().toString() + " rated " + (int) userRating[0] + " out of 5");

                firestore.collection("users").document(userId)
                        .set(data, com.google.firebase.firestore.SetOptions.merge())
                        .addOnCompleteListener(task ->
                                Toast.makeText(this, "Thanks for rating " + (int) userRating[0] + " stars!", Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
    }
}
