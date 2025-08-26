package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView foodNameText, caloriesText, proteinText, fatText, carbsText;
    private TextView healthInsightsText, processingLevelText, originText, ingredientsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        foodNameText = findViewById(R.id.foodNameText);
        caloriesText = findViewById(R.id.caloriesText);
        proteinText = findViewById(R.id.proteinText);
        fatText = findViewById(R.id.fatText);
        carbsText = findViewById(R.id.carbsText);
        healthInsightsText = findViewById(R.id.healthInsightsText);
        processingLevelText = findViewById(R.id.processingLevelText);
        originText = findViewById(R.id.originText);
        ingredientsText = findViewById(R.id.ingredientsText);

        Intent intent = getIntent();
        String foodName = intent.getStringExtra("foodName");
        int calories = intent.getIntExtra("calories", 0);
        float protein = intent.getFloatExtra("protein", 0f);
        float fat = intent.getFloatExtra("fat", 0f);
        float carbs = intent.getFloatExtra("carbs", 0f);
        String healthInsights = intent.getStringExtra("healthInsights");
        String processingLevel = intent.getStringExtra("processingLevel");
        String origin = intent.getStringExtra("culturalOrigin");
        String ingredients = intent.getStringExtra("ingredientBreakdown");

        foodNameText.setText("🍽 Food: " + foodName);
        caloriesText.setText("🔥 Calories: " + calories + " kcal");
        proteinText.setText("💪 Protein: " + protein + " g");
        fatText.setText("🧈 Fat: " + fat + " g");
        carbsText.setText("🍞 Carbs: " + carbs + " g");

        healthInsightsText.setText("🧠 Health Insight: " + (healthInsights != null ? healthInsights : "N/A"));
        processingLevelText.setText("⚙️ Processing Level: " + (processingLevel != null ? processingLevel : "N/A"));
        originText.setText("🌍 Origin: " + (origin != null ? origin : "N/A"));
        ingredientsText.setText("🧪 Ingredients: " + (ingredients != null ? ingredients : "N/A"));
    }
}