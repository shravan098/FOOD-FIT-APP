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

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText otpInput, newPasswordInput, confirmPasswordInput;
    private Button resetButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String phoneNumber;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);

        otpInput = findViewById(R.id.otpInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        resetButton = findViewById(R.id.resetPasswordButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        phoneNumber = getIntent().getStringExtra("phone");
        verificationId = getIntent().getStringExtra("verificationId");

        resetButton.setOnClickListener(v -> {
            String otp = otpInput.getText().toString();
            String newPass = newPasswordInput.getText().toString();
            String confirmPass = confirmPasswordInput.getText().toString();

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
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
                                                .update("password", newPass)
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(this, SignInActivity.class));
                                                    finish();
                                                });
                                    }
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show());
        });
    }
}
