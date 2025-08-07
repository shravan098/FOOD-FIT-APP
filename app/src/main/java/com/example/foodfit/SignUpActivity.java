package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput, phoneInput;
    private Button signUpButton, googleSignUpButton, facebookSignUpButton, backButton, nextButton;

    private String age, height, weight, goalWeight, gender, goalType, bmrResult;

    private boolean isSignupSuccessful = false;  // To track success before moving next

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // UI references
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        phoneInput = findViewById(R.id.phoneInput);
        signUpButton = findViewById(R.id.signupButton);
        googleSignUpButton = findViewById(R.id.googleSignInButton);
        facebookSignUpButton = findViewById(R.id.facebookSignInButton);
        backButton = findViewById(R.id.backButton);   // Add in layout
        nextButton = findViewById(R.id.nextButton);   // Add in layout

        // Get input data from InputActivity
        age = getIntent().getStringExtra("age");
        height = getIntent().getStringExtra("height");
        weight = getIntent().getStringExtra("weight");
        goalWeight = getIntent().getStringExtra("goalWeight");
        gender = getIntent().getStringExtra("gender");
        goalType = getIntent().getStringExtra("goalType");
        bmrResult = getIntent().getStringExtra("bmrResult");

        // Sign up logic
        signUpButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase signup
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();

                                // Prepare data to store in Firestore
                                Map<String, Object> userData = getStringObjectMap(username, email, phone);

                                FirebaseFirestore.getInstance().collection("users").document(uid)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "User data saved");
                                            Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show();
                                            isSignupSuccessful = true;  // allow next button
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error saving data", e);
                                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Back button → go to InputActivity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, InputActivity.class);
            startActivity(intent);
            finish();
        });

        // Next button → only proceed if sign-up was successful
        nextButton.setOnClickListener(v -> {
            if (isSignupSuccessful) {
                Intent intent = new Intent(SignUpActivity.this, FoodSearchActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please complete sign-up first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    private Map<String, Object> getStringObjectMap(String username, String email, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("age", age);
        userData.put("height", height);
        userData.put("weight", weight);
        userData.put("goalWeight", goalWeight);
        userData.put("gender", gender);
        userData.put("goalType", goalType);
        userData.put("bmrResult", bmrResult);
        return userData;
    }
}
