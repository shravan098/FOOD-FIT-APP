package com.example.foodfit;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput, phoneInput;
    private ImageView togglePassword, toggleConfirmPassword;
    private Button signUpButton, backButton;

    private String age, height, weight, goalWeight, gender, goalType, bmrResult;
    private int dailyCalorie;
    private boolean isSignupSuccessful = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        // UI refs
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        phoneInput = findViewById(R.id.phoneInput);
        togglePassword = findViewById(R.id.togglePassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);
        signUpButton = findViewById(R.id.signupButton);
        backButton = findViewById(R.id.backButton);

        // extras from InputActivity
        age = getIntent().getStringExtra("age");
        height = getIntent().getStringExtra("height");
        weight = getIntent().getStringExtra("weight");
        goalWeight = getIntent().getStringExtra("goalWeight");
        gender = getIntent().getStringExtra("gender");
        goalType = getIntent().getStringExtra("goalType");
        bmrResult = getIntent().getStringExtra("bmrResult");
        dailyCalorie = getIntent().getIntExtra("dailyCalorie", 1340);

        // Password toggle listeners
        togglePassword.setOnClickListener(v -> togglePasswordVisibility(passwordInput, togglePassword));
        toggleConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(confirmPasswordInput, toggleConfirmPassword));

        // Email signup handler
        signUpButton.setOnClickListener(v -> signUpWithEmail());

        // Back -> InputActivity
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, InputActivity.class));
            finish();
        });
    }

    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon) {
        if (passwordField.getTransformationMethod() instanceof PasswordTransformationMethod) {
            passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.ic_eye_open); // eye open icon
        } else {
            passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.ic_eye_closed); // eye closed icon
        }
        passwordField.setSelection(passwordField.getText().length()); // move cursor to end
    }

    // ---------------- Email signup ----------------
    private void signUpWithEmail() {
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

        // ---------------- Mobile Number Validation ----------------
        if (!phone.matches("[6-9][0-9]{9}")) {
            Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        // ---------------- Proceed with Firebase Signup ----------------
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserData(mAuth.getCurrentUser(), username, phone, true);
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "unknown";
                        Log.e(TAG, "Email sign-up failed: " + err);
                        Toast.makeText(this, "Sign up failed: " + err, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ---------------- Save to Firestore ----------------
    private void saveUserData(FirebaseUser firebaseUser, String username, String phone, boolean redirect) {
        if (firebaseUser == null) {
            Log.e(TAG, "saveUserData called with null user");
            return;
        }

        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username != null ? username : "");
        userData.put("email", email);
        userData.put("phone", phone != null ? phone : "");
        userData.put("age", age != null ? age : "");
        userData.put("height", height != null ? height : "");
        userData.put("weight", weight != null ? weight : "");
        userData.put("goalWeight", goalWeight != null ? goalWeight : "");
        userData.put("gender", gender != null ? gender : "");
        userData.put("goalType", goalType != null ? goalType : "");
        userData.put("bmrResult", bmrResult != null ? bmrResult : "");
        userData.put("dailyCalorie", dailyCalorie);

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved to Firestore for uid=" + uid);
                    Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show();
                    isSignupSuccessful = true;
                    if (redirect) {
                        Intent intent = new Intent(SignUpActivity.this, LoadingActivity.class);
                        intent.putExtra("dailyCalorie", dailyCalorie);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data", e);
                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
