package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlanSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_selection);

        Button loseWeightBtn = findViewById(R.id.loseWeightButton);
        Button maintainWeightBtn = findViewById(R.id.maintainWeightButton);
        Button gainWeightBtn = findViewById(R.id.gainWeightButton);
        TextView signInLink = findViewById(R.id.signInLink);

        loseWeightBtn.setOnClickListener(view -> openNextScreen("Lose Weight"));
        maintainWeightBtn.setOnClickListener(view -> openNextScreen("Maintain Weight"));
        gainWeightBtn.setOnClickListener(view -> openNextScreen("Gain Weight"));

        signInLink.setOnClickListener(view -> {
            Intent intent = new Intent(PlanSelectionActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void openNextScreen(String goalType) {
        Intent intent = new Intent(PlanSelectionActivity.this, InputActivity.class);
        intent.putExtra("goal", goalType);
        startActivity(intent);
        finish();
    }
}