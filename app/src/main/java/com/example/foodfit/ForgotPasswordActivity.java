package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText phoneInput;
    private Button getOtpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        phoneInput = findViewById(R.id.phoneInput);
        getOtpButton = findViewById(R.id.getOtpButton);

        getOtpButton.setOnClickListener(v -> {
            phoneNumber = phoneInput.getText().toString();

            db.collection("users")
                    .whereEqualTo("phone", phoneNumber)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            Intent intent = new Intent(this, ResetPasswordActivity.class);
                            intent.putExtra("phone", phoneNumber);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

