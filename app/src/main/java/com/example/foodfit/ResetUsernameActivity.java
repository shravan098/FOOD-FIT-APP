package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResetUsernameActivity extends AppCompatActivity {

    private EditText otpInput, newUsernameInput;
    private Button resetButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String phoneNumber, verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_username_activity);

        otpInput = findViewById(R.id.otpInput);
        newUsernameInput = findViewById(R.id.newUsernameInput);
        resetButton = findViewById(R.id.resetUsernameButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        phoneNumber = getIntent().getStringExtra("phone");
        verificationId = getIntent().getStringExtra("verificationId");

        resetButton.setOnClickListener(v -> {
            String otp = otpInput.getText().toString().trim();
            String newUsername = newUsernameInput.getText().toString().trim();

            if (otp.isEmpty() || newUsername.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

            mAuth.signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> {
                        db.collection("users")
                                .whereEqualTo("phone", phoneNumber)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    if (!snapshot.isEmpty()) {
                                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                                        db.collection("users").document(doc.getId())
                                                .update("username", newUsername)
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(this, SignInActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
                                    } else {
                                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show());
        });
    }
}
