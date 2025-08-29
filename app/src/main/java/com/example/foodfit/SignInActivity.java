package com.example.foodfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private TextView forgotPasswordText;
    private Button signInButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        signInButton = findViewById(R.id.signInButton);

        TextView forgotUsernameText = findViewById(R.id.UserPasswordText);

        forgotUsernameText.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotUsernameActivity.class));
        });

        forgotPasswordText.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        signInButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            String email = doc.getString("email");

                            mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "Signed In!", Toast.LENGTH_SHORT).show();

                                            // ✅ Save login info in SharedPreferences
                                            SharedPreferences prefs = getSharedPreferences("FoodFitPrefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putBoolean("isSignedUp", true); // FIXED
                                            editor.putLong("lastLoginTime", System.currentTimeMillis());
                                            editor.apply();

                                            // ✅ Fetch user data and send to LoadingActivity
                                            db.collection("users").document(doc.getId())
                                                    .get()
                                                    .addOnSuccessListener(userDoc -> {
                                                        if (userDoc.exists()) {
                                                            int dailyCalorie = getIntSafe(userDoc, "dailyCalorie", 1340);
                                                            int age = getIntSafe(userDoc, "age", 0);
                                                            int height = getIntSafe(userDoc, "height", 0);
                                                            int weight = getIntSafe(userDoc, "weight", 0);
                                                            int goalWeight = getIntSafe(userDoc, "goalWeight", 0);

                                                            String goalType = userDoc.getString("goalType");
                                                            String gender = userDoc.getString("gender");

                                                            Intent intent = new Intent(SignInActivity.this, LoadingActivity.class);
                                                            intent.putExtra("dailyCalorie", dailyCalorie);
                                                            intent.putExtra("goalType", goalType);
                                                            intent.putExtra("age", age);
                                                            intent.putExtra("height", height);
                                                            intent.putExtra("weight", weight);
                                                            intent.putExtra("goalWeight", goalWeight);
                                                            intent.putExtra("gender", gender);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });

                                        } else {
                                            Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private int getIntSafe(DocumentSnapshot doc, String field, int defaultValue) {
        Object value = doc.get(field);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
