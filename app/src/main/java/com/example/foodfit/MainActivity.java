package com.example.foodfit;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView resultView = findViewById(R.id.resultView);
        resultView.setText(getString(R.string.searching_text));

        // Get data from intent
        String foodDescription = getIntent().getStringExtra("foodDescription");
        String nutrientJson = getIntent().getStringExtra("nutrients");

        Log.d(TAG, "Received description: " + foodDescription);
        Log.d(TAG, "Received nutrient JSON: " + nutrientJson);

        List<USDANutrient> nutrients = null;
        if (nutrientJson != null && !nutrientJson.isEmpty()) {
            nutrients = new Gson().fromJson(
                    nutrientJson,
                    new TypeToken<List<USDANutrient>>() {}.getType()
            );
        }

        if (nutrients != null && !nutrients.isEmpty()) {
            // ✅ Show nutrients normally
            resultView.setText(formatKeyNutrients(foodDescription, nutrients));
        } else {
            // ✅ No data passed → show friendly message instead of error
            resultView.setText("🔎 Please search for a food item to see nutrients");
            Log.w(TAG, "No nutrient data passed, waiting for user to search");
        }
    }

    private String formatKeyNutrients(String description, List<USDANutrient> foodNutrients) {
        StringBuilder builder = new StringBuilder("🥗 " + description + "\n\n");

        for (USDANutrient nutrient : foodNutrients) {
            String name = nutrient.getNutrientName().toLowerCase();
            if (name.contains("protein") || name.contains("fat") || name.contains("carbohydrate") ||
                    name.contains("fiber") || name.contains("energy") || name.contains("calorie")) {

                builder.append("   🔹 ")
                        .append(nutrient.getNutrientName())
                        .append(": ")
                        .append(nutrient.getValue())
                        .append("\n");
            }
        }

        return builder.toString();
    }
}
