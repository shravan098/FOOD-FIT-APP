package com.example.foodfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 sec splash
    private static final long EXPIRY_DURATION = 7 * 24 * 60 * 60 * 1000L; // 7 days

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("FoodFitPrefs", MODE_PRIVATE);
            boolean isSignedUp = prefs.getBoolean("isSignedUp", false);
            long lastLogin = prefs.getLong("lastLoginTime", 0);
            long now = System.currentTimeMillis();

            if (!isSignedUp) {
                // ✅ Naya user
                startActivity(new Intent(this, PlanSelectionActivity.class));
                finish();
            } else {
                // ✅ Firestore me user exist karta hai ya nahi check karo
                String userId = FirebaseAuth.getInstance().getUid();
                if (userId != null) {
                    FirebaseFirestore.getInstance().collection("users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                Intent intent;
                                if (doc.exists()) {
                                    if (now - lastLogin < EXPIRY_DURATION) {
                                        intent = new Intent(SplashActivity.this, Postlogin.class);
                                    } else {
                                        intent = new Intent(SplashActivity.this, SignInActivity.class);
                                    }
                                } else {
                                    prefs.edit().clear().apply();
                                    intent = new Intent(SplashActivity.this, PlanSelectionActivity.class);
                                }
                                startActivity(intent);
                                finish();
                            })

                            .addOnFailureListener(e -> {
                                // Agar fetch error hua to SignIn bhej do
                                startActivity(new Intent(this, SignInActivity.class));
                                finish();
                            });
                } else {
                    // Agar FirebaseAuth ka user null hai → SignIn
                    startActivity(new Intent(this, SignInActivity.class));
                    finish();
                }
            }
        }, SPLASH_DELAY);
    }
}
