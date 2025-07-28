package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class InputActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input); // This should match your XML file name

        // Back button logic
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(InputActivity.this, PlanSelectionActivity.class); // Replace with your actual activity
            startActivity(intent);
            finish(); // Optional: closes this screen
        });

        // Next button logic
        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(InputActivity.this, FoodSearchActivity.class); // Replace with your actual activity
            startActivity(intent);
            finish(); // Optional again
        });
    }
}