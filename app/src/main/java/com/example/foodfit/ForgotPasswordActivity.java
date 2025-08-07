package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText phoneInput;
    private Button getOtpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String verificationId;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_activity);

        phoneInput = findViewById(R.id.phoneInput);
        getOtpButton = findViewById(R.id.getOtpButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        getOtpButton.setOnClickListener(v -> {
            phoneNumber = phoneInput.getText().toString();

            db.collection("users")
                    .whereEqualTo("phone", phoneNumber)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            sendOtp(phoneNumber);
                        } else {
                            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {
                                // Optional: Handle auto-verification
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                Toast.makeText(ForgotPasswordActivity.this, "OTP send failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                                ForgotPasswordActivity.this.verificationId = verificationId;

                                Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                                intent.putExtra("verificationId", verificationId);
                                intent.putExtra("phone", phoneNumber);
                                startActivity(intent);
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}
