package com.example.foodfit;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;


import java.text.SimpleDateFormat;
import java.util.*;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3500; // 3.5 seconds

    @Override
    protected void onResume() {
        super.onResume();
        SessionTracker.startTime = System.currentTimeMillis();
        SessionTracker.actions.clear();
        SessionTracker.actions.add("App opened at: " + formatTime(SessionTracker.startTime));
    }

    @Override
    protected void onPause() {
        super.onPause();
        SessionTracker.endTime = System.currentTimeMillis();
        SessionTracker.actions.add("App closed at: " + formatTime(SessionTracker.endTime));
        saveSessionToFirebase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, PlanSelectionActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }

    private void saveSessionToFirebase() {
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("startTime", SessionTracker.startTime);
        sessionData.put("endTime", SessionTracker.endTime);
        sessionData.put("duration", SessionTracker.endTime - SessionTracker.startTime);
        sessionData.put("actions", SessionTracker.actions);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sessionData.put("date", sdf.format(new Date(SessionTracker.startTime)));

        // ✅ Enhanced metadata
        String sessionId = UUID.randomUUID().toString();
        sessionData.put("sessionId", sessionId);
        sessionData.put("deviceModel", Build.MODEL);
        sessionData.put("deviceBrand", Build.BRAND);
        sessionData.put("deviceOS", Build.VERSION.RELEASE);
        sessionData.put("userId", "guest_" + System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("userSessions")
                .document(sessionId)
                .set(sessionData)
                .addOnSuccessListener(aVoid -> {
                    // Optional: Logging or analytics
                })
                .addOnFailureListener(e -> {
                    // Optional: Error handling or retry logic
                });
    }

    private String formatTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}