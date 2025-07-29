package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            SessionTracker.actions.add("Clicked Back button at " + System.currentTimeMillis());
            Intent intent = new Intent(InputActivity.this, PlanSelectionActivity.class);
            startActivity(intent);
            finish();
        });

        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            SessionTracker.actions.add("Clicked Next button at " + System.currentTimeMillis());
            Intent intent = new Intent(InputActivity.this, FoodSearchActivity.class);
            startActivity(intent);
            finish();
        });
    }
}