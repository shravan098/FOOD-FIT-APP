package com.example.foodfit;

import android.content.Intent;
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
        setContentView(R.layout.sign_in_activity); // layout should match your XML

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

                                            // ✅ Redirect to FoodSearchActivity
                                            Intent intent = new Intent(SignInActivity.this, FoodSearchActivity.class);
                                            startActivity(intent);
                                            finish();

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

        // Google / Facebook sign-in behavior can be added here if needed
    }
}
