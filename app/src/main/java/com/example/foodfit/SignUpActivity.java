package com.example.foodfit;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput, phoneInput;
    private TextView statusText;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        phoneInput = findViewById(R.id.phoneInput);
        statusText = findViewById(R.id.statusText);
        Button signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
            statusText.setText("All fields are required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            statusText.setText("Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            statusText.setText("Password must be at least 6 characters.");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("username", username);
                        userMap.put("email", email);
                        userMap.put("phone", phone);
                        userMap.put("uid", uid);

                        userMap.put("goal", getIntent().getStringExtra("goal"));
                        userMap.put("age", getIntent().getStringExtra("age"));
                        userMap.put("height", getIntent().getStringExtra("height"));
                        userMap.put("weight", getIntent().getStringExtra("weight"));
                        userMap.put("goalWeight", getIntent().getStringExtra("goalWeight"));
                        userMap.put("gender", getIntent().getStringExtra("gender"));

                        firestore.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener(unused -> {
                                    statusText.setTextColor(Color.GREEN);
                                    statusText.setText(R.string.sign_up_successful);
                                })
                                .addOnFailureListener(e -> {
                                    statusText.setText("Firestore Error: " + e.getMessage());
                                });
                    } else {
                        statusText.setText("Sign-Up Failed: " + task.getException().getMessage());
                    }
                });
    }
}
