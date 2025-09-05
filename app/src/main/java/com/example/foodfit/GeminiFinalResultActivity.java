package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GeminiFinalResultActivity extends AppCompatActivity {

    TextView foodNameText, caloriesText, proteinText, fatText, carbsText,
            healthInsightsText, processingLevelText, culturalOriginText, ingredientBreakdownText;
    EditText weightInput;
    Button addToMealBtn;

    private double baseCalories, baseProtein, baseFat, baseCarbs;
    private String foodName;
    private String docId;
    private String mealType;  // ✅ Breakfast / Lunch / Dinner

    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geminifinalresult);

        // Bind views
        foodNameText = findViewById(R.id.foodNameText);
        caloriesText = findViewById(R.id.caloriesText);
        proteinText = findViewById(R.id.proteinText);
        fatText = findViewById(R.id.fatText);
        carbsText = findViewById(R.id.carbsText);
        weightInput = findViewById(R.id.weightInput);
        addToMealBtn = findViewById(R.id.addToMealBtn);

        healthInsightsText = findViewById(R.id.healthInsightsText);
        processingLevelText = findViewById(R.id.processingLevelText);
        culturalOriginText = findViewById(R.id.culturalOriginText);
        ingredientBreakdownText = findViewById(R.id.ingredientBreakdownText);

        // Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get data from Intent
        docId = getIntent().getStringExtra("docId");
        foodName = getIntent().getStringExtra("foodName");
        mealType = getIntent().getStringExtra("mealType"); // ✅ ensure passed

        if (mealType == null || mealType.trim().isEmpty()) {
            Toast.makeText(this, "⚠️ Meal type not received, defaulting to Breakfast", Toast.LENGTH_SHORT).show();
            mealType = "Breakfast"; // fallback
        }

        double grams = getIntent().getDoubleExtra("grams", 100);

        double caloriesVal = parseDouble(getIntent().getStringExtra("calories"));
        double proteinVal = parseDouble(getIntent().getStringExtra("protein"));
        double fatVal = parseDouble(getIntent().getStringExtra("fat"));
        double carbsVal = parseDouble(getIntent().getStringExtra("carbs"));

        // Normalize to per 100g
        if (grams > 0) {
            baseCalories = (caloriesVal / grams) * 100.0;
            baseProtein = (proteinVal / grams) * 100.0;
            baseFat = (fatVal / grams) * 100.0;
            baseCarbs = (carbsVal / grams) * 100.0;
        } else {
            baseCalories = caloriesVal;
            baseProtein = proteinVal;
            baseFat = fatVal;
            baseCarbs = carbsVal;
        }

        String healthInsights = getIntent().getStringExtra("healthInsights");
        String processingLevel = getIntent().getStringExtra("processingLevel");
        String culturalOrigin = getIntent().getStringExtra("culturalOrigin");
        String ingredientBreakdown = getIntent().getStringExtra("ingredientBreakdown");

        // Set UI
        foodNameText.setText("🍽 Food: " + foodName);
        weightInput.setText(String.valueOf((int) grams));
        updateNutritionViews(grams);

        healthInsightsText.setText(healthInsights != null ? healthInsights : "");
        processingLevelText.setText("Processing: " + (processingLevel != null ? processingLevel : ""));
        culturalOriginText.setText("Cultural Origin: " + (culturalOrigin != null ? culturalOrigin : ""));
        ingredientBreakdownText.setText("Ingredients: " + (ingredientBreakdown != null ? ingredientBreakdown : ""));

        // Weight change listener
        weightInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    updateNutritionViews(100);
                } else {
                    try {
                        updateNutritionViews(Double.parseDouble(s.toString()));
                    } catch (NumberFormatException e) {
                        updateNutritionViews(100);
                    }
                }
            }
        });

        // Add / Update button
        addToMealBtn.setOnClickListener(v -> saveMealToFirestore());
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void updateNutritionViews(double grams) {
        double factor = grams / 100.0;
        caloriesText.setText("Calories: " + String.format("%.2f", baseCalories * factor) + " kcal");
        proteinText.setText("Protein: " + String.format("%.2f", baseProtein * factor) + " g");
        fatText.setText("Fat: " + String.format("%.2f", baseFat * factor) + " g");
        carbsText.setText("Carbs: " + String.format("%.2f", baseCarbs * factor) + " g");
    }

    private void saveMealToFirestore() {
        String gramsStr = weightInput.getText().toString().trim();
        double grams = gramsStr.isEmpty() ? 100 : Double.parseDouble(gramsStr);
        double factor = grams / 100.0;

        Map<String, Object> mealData = new HashMap<>();
        mealData.put("foodName", foodName);
        mealData.put("calories", String.format("%.2f", baseCalories * factor));
        mealData.put("protein", String.format("%.2f", baseProtein * factor));
        mealData.put("fat", String.format("%.2f", baseFat * factor));
        mealData.put("carbs", String.format("%.2f", baseCarbs * factor));
        mealData.put("grams", grams);
        mealData.put("mealType", mealType);  // ✅ correct type save

        if (docId != null) {
            db.collection("users").document(userId)
                    .collection("meals").document(docId)
                    .update(mealData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✅ Meal updated in " + mealType, Toast.LENGTH_SHORT).show();
                        goBackToMeal();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            db.collection("users").document(userId)
                    .collection("meals").add(mealData)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "✅ Added to " + mealType, Toast.LENGTH_SHORT).show();
                        goBackToMeal();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void goBackToMeal() {
        Intent intent;
        switch (mealType) {
            case "Lunch":
                intent = new Intent(GeminiFinalResultActivity.this, Lunchmealactivity.class);
                break;
            case "Dinner":
                intent = new Intent(GeminiFinalResultActivity.this, DinnerMealActivity.class);
                break;
            default:
                intent = new Intent(GeminiFinalResultActivity.this, BreakfastMealActivity.class);
                break;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
