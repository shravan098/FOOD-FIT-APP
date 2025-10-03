package com.example.foodfit;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView foodNameView, resultView;
    private EditText weightInput;
    private Button recalcButton, addMealButton;
    private RecyclerView mealRecycler;

    private List<Nutrient> nutrients;
    private String foodDescription;
    private String mealType;

    private FirebaseFirestore firestore;
    private String userId;
    private List<DocumentSnapshot> mealList = new ArrayList<>();
    private MealAdapter mealAdapter;

    // ✅ New fields for countable foods
    private String unit = "g"; // "g" = grams, "piece" = countable items
    private double avgWeightPerPiece = 100.0; // average grams per piece (default)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind views
        foodNameView = findViewById(R.id.foodName);
        resultView = findViewById(R.id.resultView);
        weightInput = findViewById(R.id.weightInput);
        recalcButton = findViewById(R.id.recalcButton);
        addMealButton = findViewById(R.id.addMealButton);
        mealRecycler = findViewById(R.id.mealRecycler);

        // Check Firebase user login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();

        // Get Intent data
        foodDescription = getIntent().getStringExtra("foodDescription");
        mealType = getIntent().getStringExtra("mealType");

        if (mealType == null || mealType.trim().isEmpty()) {
            Toast.makeText(this, "❌ Meal type missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String nutrientJson = getIntent().getStringExtra("nutrients");
        if (nutrientJson != null && !nutrientJson.isEmpty()) {
            nutrients = new Gson().fromJson(nutrientJson, new TypeToken<List<Nutrient>>() {}.getType());
        }

        // ✅ Detect unit automatically
        unit = "g"; // default
        avgWeightPerPiece = 100.0;

        if (foodDescription != null) {
            String desc = foodDescription.toLowerCase();

            // Detect countable items
            if (desc.contains("apple") || desc.contains("mango") || desc.contains("banana")
                    || desc.contains("egg") || desc.contains("piece") || desc.contains("unit")
                    || desc.contains("item") || desc.contains("slice") || desc.contains("bread")) {

                unit = "piece";
                avgWeightPerPiece = 100.0; // default, can improve if API gives portion weight
            }
        }

        // ✅ Adjust input hint
        if (unit.equals("piece")) {
            weightInput.setHint("Enter pieces");
            weightInput.setText("1");
        } else {
            weightInput.setHint("Enter weight (g)");
            weightInput.setText("100");
        }

        // Setup RecyclerView
        mealRecycler.setLayoutManager(new LinearLayoutManager(this));
        mealAdapter = new MealAdapter(this, mealList, new MealAdapter.OnMealActionListener() {
            @Override
            public void onDelete(DocumentSnapshot doc) {
                int index = mealList.indexOf(doc);
                doc.getReference().delete()
                        .addOnSuccessListener(aVoid -> {
                            mealList.remove(doc);
                            mealAdapter.notifyItemRemoved(index);
                            Toast.makeText(MainActivity.this, "Meal deleted", Toast.LENGTH_SHORT).show();
                        });
            }
        });
        mealRecycler.setAdapter(mealAdapter);

        // Show default values
        updateNutritionDisplay(unit.equals("piece") ? 1 : 100);

        // Recalculate nutrients
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

        // Add meal
        addMealButton.setOnClickListener(v -> saveMealToFirestore());

        // Load existing meals
        loadMealsFromFirestore();
    }

    private Map<String, Double> calculateNutrition(double grams) {
        double calories = 0, protein = 0, carbs = 0, fat = 0, fiber = 0;

        if (nutrients != null) {
            for (Nutrient nutrient : nutrients) {
                String name = nutrient.getNutrientName().toLowerCase();
                double scaled = nutrient.getValue() * grams / 100.0;

                if (name.contains("energy") || name.contains("calorie")) calories = scaled;
                else if (name.contains("protein") && !name.contains("amino")) protein = scaled;
                else if (name.contains("carbohydrate")) carbs = scaled;
                else if (name.contains("total lipid")) fat = scaled;
                else if (name.contains("fiber")) fiber = scaled;
            }
        }

        Map<String, Double> result = new HashMap<>();
        result.put("calories", calories);
        result.put("protein", protein);
        result.put("carbs", carbs);
        result.put("fat", fat);
        result.put("fiber", fiber);
        return result;
    }

    private void updateNutritionDisplay(double inputValue) {
        if (nutrients == null || nutrients.isEmpty()) {
            resultView.setText("No nutrient data available ❌");
            foodNameView.setText(foodDescription);
            return;
        }

        double grams = unit.equals("piece") ? inputValue * avgWeightPerPiece : inputValue;

        Map<String, Double> nutrition = calculateNutrition(grams);

        foodNameView.setText(foodDescription + "    🔥 " + Math.round(nutrition.get("calories")) + " kcal");

        StringBuilder builder = new StringBuilder();
        if (unit.equals("piece")) {
            builder.append("Pieces: ").append(inputValue).append("\n\n");
        } else {
            builder.append("Weight: ").append(inputValue).append(" g\n\n");
        }
        builder.append("💪 Protein: ").append(roundOne(nutrition.get("protein"))).append(" g\n");
        builder.append("🍞 Carbs: ").append(roundOne(nutrition.get("carbs"))).append(" g\n");
        builder.append("🥑 Fat: ").append(roundOne(nutrition.get("fat"))).append(" g\n");
        builder.append("🌿 Fiber: ").append(roundOne(nutrition.get("fiber"))).append(" g\n");

        resultView.setText(builder.toString());
    }

    private double roundOne(double val) {
        return Math.round(val * 10.0) / 10.0;
    }

    private void saveMealToFirestore() {
        double input = unit.equals("piece") ? 1 : 100;
        try {
            input = Double.parseDouble(weightInput.getText().toString().trim());
        } catch (NumberFormatException ignored) {}

        double grams = unit.equals("piece") ? input * avgWeightPerPiece : input;

        Map<String, Double> nutrition = calculateNutrition(grams);

        Map<String, Object> mealData = new HashMap<>();
        mealData.put("foodName", foodDescription);
        mealData.put("calories", Math.round(nutrition.get("calories")));
        mealData.put("protein", roundOne(nutrition.get("protein")));
        mealData.put("carbs", roundOne(nutrition.get("carbs")));
        mealData.put("fat", roundOne(nutrition.get("fat")));
        mealData.put("fiber", roundOne(nutrition.get("fiber")));
        mealData.put("weight", input);
        mealData.put("unit", unit); // ✅ Store unit type
        mealData.put("mealType", mealType);

        firestore.collection("users")
                .document(userId)
                .collection("meals")
                .add(mealData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Added to " + mealType + "!", Toast.LENGTH_SHORT).show();
                    loadMealsFromFirestore();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadMealsFromFirestore() {
        firestore.collection("users")
                .document(userId)
                .collection("meals")
                .whereEqualTo("mealType", mealType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mealList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        if (doc.get("calories") != null) {
                            mealList.add(doc);
                        }
                    }
                    mealAdapter.notifyDataSetChanged();
                });
    }
}
