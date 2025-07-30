package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InputActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        SessionTracker.actions.add("Entered InputActivity at " + System.currentTimeMillis());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input);

        EditText ageInput = findViewById(R.id.ageInput);
        EditText heightInput = findViewById(R.id.heightInput);
        EditText weightInput = findViewById(R.id.weightInput);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            SessionTracker.actions.add("Clicked Back button at " + System.currentTimeMillis());
            Intent intent = new Intent(InputActivity.this, PlanSelectionActivity.class);
            startActivity(intent);
            finish();
        });

        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            String age = ageInput.getText().toString().trim();
            String height = heightInput.getText().toString().trim();
            String weight = weightInput.getText().toString().trim();

            SessionTracker.actions.add("Entered Age: " + age);
            SessionTracker.actions.add("Entered Height: " + height);
            SessionTracker.actions.add("Entered Weight: " + weight);
            SessionTracker.actions.add("Clicked Next button at " + System.currentTimeMillis());

            // 🔥 Firestore upload
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("actions", new ArrayList<>(SessionTracker.actions));
            sessionData.put("timestamp", System.currentTimeMillis());

            db.collection("userSessions")
                    .add(sessionData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Session saved with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error saving session", e);
                    });

            Intent intent = new Intent(InputActivity.this, FoodSearchActivity.class);
            startActivity(intent);
            finish();
        });
    }
}