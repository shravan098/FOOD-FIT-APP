package com.example.foodfit;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoadingActivity2 extends AppCompatActivity {

    private TextView foodNameView, resultView;
    private EditText weightInput;
    private Button recalcButton, addMealButton;

    private ArrayList<FoodNutrition> foodList = new ArrayList<>();
    private String mealType;
    private FoodNutrition selectedFood;

    private FirebaseFirestore firestore;
    private String userId;

    private String unit = "g"; // default grams
    private double avgWeightPerPiece = 100.0; // default per-piece assumption

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadingactivity2);

        // Bind views
        foodNameView = findViewById(R.id.foodName);
        resultView = findViewById(R.id.resultView);
        weightInput = findViewById(R.id.weightInput);
        recalcButton = findViewById(R.id.recalcButton);
        addMealButton = findViewById(R.id.addMealButton);

        // Check Firebase user
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();

        // Get intent data
        mealType = getIntent().getStringExtra("mealType");
        String geminiResponse = getIntent().getStringExtra("geminiResponse");

        if (mealType == null || mealType.trim().isEmpty()) {
            Toast.makeText(this, "❌ Meal type missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (geminiResponse != null && !geminiResponse.isEmpty()) {
            parseGeminiResponse(geminiResponse);
        }

        // Pick first detected food
        if (!foodList.isEmpty()) {
            selectedFood = foodList.get(0);
        }

        if (selectedFood == null) {
            Toast.makeText(this, "No food detected ❌", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Detect if food is countable (piece vs grams)
        String desc = selectedFood.getFoodLabel().toLowerCase();
        if (desc.contains("apple") || desc.contains("banana") || desc.contains("mango")
                || desc.contains("egg") || desc.contains("slice") || desc.contains("bread")) {
            unit = "piece";
            avgWeightPerPiece = 100.0;
        }

        // Adjust hint
        if (unit.equals("piece")) {
            weightInput.setHint("Enter pieces");
            weightInput.setText("1");
        } else {
            weightInput.setHint("Enter weight (g)");
            weightInput.setText("100");
        }

        // Default update
        updateNutritionDisplay(unit.equals("piece") ? 1 : 100);

        // Recalc button
        recalcButton.setOnClickListener(v -> {
            String input = weightInput.getText().toString().trim();
            if (!input.isEmpty()) {
                try {
                    double value = Double.parseDouble(input);
                    weightInput.setError(null);
                    updateNutritionDisplay(value);
                } catch (NumberFormatException e) {
                    weightInput.setError("Enter valid number");
                }
            }
        });

        // Add meal button
        addMealButton.setOnClickListener(v -> saveMealToFirestore());
    }

    private void parseGeminiResponse(String responseStr) {
        try {
            responseStr = responseStr.trim();
            if (responseStr.startsWith("```")) {
                int firstBrace = responseStr.indexOf("[");
                int lastBrace = responseStr.lastIndexOf("]");
                if (firstBrace != -1 && lastBrace != -1) {
                    responseStr = responseStr.substring(firstBrace, lastBrace + 1);
                    Log.d("GeminiRaw", "Raw Gemini Response: " + responseStr);
                }
            }

            JSONArray array = new JSONArray(responseStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String foodLabel = obj.optString("foodLabel", "Unknown");
                double calories = obj.optDouble("calories", 0);
                double protein = obj.optDouble("protein", 0);
                double fat = obj.optDouble("fat", 0);
                double carbs = obj.optDouble("carbs", 0);
                double sugar = obj.optDouble("sugar", 0);
                double fiber = obj.optDouble("fiber", 0);
                double sodium = obj.optDouble("sodium", 0);
                double cholesterol = obj.optDouble("cholesterol", 0);
                double saturatedFat = obj.optDouble("saturatedFat", 0);
                double transFat = obj.optDouble("transFat", 0);
                double addedSugars = obj.optDouble("addedSugars", 0);
                String vitamins = obj.optString("vitamins", "N/A");
                String minerals = obj.optString("minerals", "N/A");
                String glycemicIndex = obj.optString("glycemicIndex", "N/A");
                String allergens = obj.optString("allergens", "None");
                String verdict = obj.optString("verdict", "Eat in moderation");
                String ingredients = obj.optString("ingredients", "N/A");
                String servingSize = obj.optString("servingSize", "100g");

                foodList.add(new FoodNutrition(foodLabel, calories, protein, fat, carbs,
                        sugar, fiber, sodium, cholesterol, saturatedFat, transFat, addedSugars,
                        vitamins, minerals, glycemicIndex, allergens, verdict,
                        ingredients, servingSize));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gemini parse error", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNutritionDisplay(double inputValue) {
        if (selectedFood == null) {
            resultView.setText("No food data available ❌");
            return;
        }

        double grams = unit.equals("piece") ? inputValue * avgWeightPerPiece : inputValue;

        // Scale nutrients (Gemini values assumed per 100g)
        double scale = grams / 100.0;
        double calories = selectedFood.getCalories() * scale;
        double protein = selectedFood.getProtein() * scale;
        double fat = selectedFood.getFat() * scale;
        double carbs = selectedFood.getCarbs() * scale;
        double sugar = selectedFood.getSugar() * scale;
        double fiber = selectedFood.getFiber() * scale;
        double sodium = selectedFood.getSodium() * scale;
        double cholesterol = selectedFood.getCholesterol() * scale;
        double satFat = selectedFood.getSaturatedFat() * scale;
        double transFat = selectedFood.getTransFat() * scale;
        double addedSugars = selectedFood.getAddedSugars() * scale;

        // Show name + calories
        foodNameView.setText(selectedFood.getFoodLabel() + " 🔥 " + Math.round(calories) + " kcal");

        StringBuilder builder = new StringBuilder();
        if (unit.equals("piece")) {
            builder.append("Pieces: ").append(inputValue).append("\n\n");
        } else {
            builder.append("Weight: ").append(inputValue).append(" g\n\n");
        }

        // Macros
        builder.append("💪 Protein: ").append(roundOne(protein)).append(" g\n");
        builder.append("🍞 Carbs: ").append(roundOne(carbs)).append(" g\n");
        builder.append("🥑 Fat: ").append(roundOne(fat)).append(" g\n");
        builder.append("🍬 Sugar: ").append(roundOne(sugar)).append(" g\n");
        builder.append("🌾 Fiber: ").append(roundOne(fiber)).append(" g\n\n");

        // Health-related
        builder.append("🧂 Sodium: ").append(roundOne(sodium)).append(" mg\n");
        builder.append("❤️ Cholesterol: ").append(roundOne(cholesterol)).append(" mg\n");
        builder.append("🥓 Sat. Fat: ").append(roundOne(satFat)).append(" g\n");
        builder.append("❌ Trans Fat: ").append(roundOne(transFat)).append(" g\n");
        builder.append("➕ Added Sugars: ").append(roundOne(addedSugars)).append(" g\n\n");

        // Extra
        builder.append("💊 Vitamins: ").append(selectedFood.getVitamins()).append("\n");
        builder.append("🪨 Minerals: ").append(selectedFood.getMinerals()).append("\n");
        builder.append("📊 Glycemic Index: ").append(selectedFood.getGlycemicIndex()).append("\n");
        builder.append("⚠️ Allergens: ").append(selectedFood.getAllergens()).append("\n\n");

        // Meta
        builder.append("📏 Serving Size: ").append(selectedFood.getServingSize()).append("\n");
        builder.append("🧾 Ingredients: ").append(selectedFood.getIngredients()).append("\n\n");

        // Verdict
        builder.append("✅ Verdict: ").append(selectedFood.getVerdict()).append("\n");

        resultView.setText(builder.toString());
    }

    private void saveMealToFirestore() {
        double input = unit.equals("piece") ? 1 : 100;
        try {
            input = Double.parseDouble(weightInput.getText().toString().trim());
        } catch (NumberFormatException ignored) {}

        double grams = unit.equals("piece") ? input * avgWeightPerPiece : input;

        double scale = grams / 100.0;
        double calories = selectedFood.getCalories() * scale;
        double protein = selectedFood.getProtein() * scale;
        double fat = selectedFood.getFat() * scale;
        double carbs = selectedFood.getCarbs() * scale;

        Map<String, Object> mealData = new HashMap<>();
        mealData.put("foodName", selectedFood.getFoodLabel());
        mealData.put("calories", Math.round(calories));
        mealData.put("protein", roundOne(protein));
        mealData.put("carbs", roundOne(carbs));
        mealData.put("fat", roundOne(fat));
        mealData.put("weight", input);
        mealData.put("unit", unit);
        mealData.put("mealType", mealType);

        firestore.collection("users")
                .document(userId)
                .collection("meals")
                .add(mealData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Added to " + mealType + "!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private double roundOne(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
