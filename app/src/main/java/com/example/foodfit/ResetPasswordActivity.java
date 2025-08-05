package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText newPasswordInput, confirmPasswordInput;
    private Button resetButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        resetButton = findViewById(R.id.resetPasswordButton);

        phoneNumber = getIntent().getStringExtra("phone");

        resetButton.setOnClickListener(v -> {
            String newPass = newPasswordInput.getText().toString();
            String confirmPass = confirmPasswordInput.getText().toString();

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("phone", phoneNumber)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            DocumentSnapshot doc = snapshot.getDocuments().get(0);
                            String email = doc.getString("email");

                            mAuth.sendPasswordResetEmail(email)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Reset link sent to email", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, SignInActivity.class));
                                    });
                        }
                    });
        });
    }
}
