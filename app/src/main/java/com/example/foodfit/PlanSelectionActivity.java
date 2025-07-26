package com.example.foodfit; // 🔄 Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class PlanSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_selection);

        // 🔌 Connect buttons
        Button loseWeightBtn = findViewById(R.id.loseWeightButton);
        Button maintainWeightBtn = findViewById(R.id.maintainWeightButton);
        Button gainWeightBtn = findViewById(R.id.gainWeightButton);

        // 💡 Handle button clicks
        loseWeightBtn.setOnClickListener(view -> openNextScreen("Lose Weight"));
        maintainWeightBtn.setOnClickListener(view -> openNextScreen("Maintain Weight"));
        gainWeightBtn.setOnClickListener(view -> openNextScreen("Gain Weight"));
    }

    // 🧭 Function to navigate forward with selected goal
    private void openNextScreen(String goalType) {
        Intent intent = new Intent(PlanSelectionActivity.this, FoodSearchActivity.class);
        intent.putExtra("goal", goalType);
        startActivity(intent);
        finish();
    }
}