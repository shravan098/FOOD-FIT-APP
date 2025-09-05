package com.example.foodfit;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseFoodResponse {

    public static FoodResponse fromGeminiRaw(String rawText) {
        FoodResponse response = new FoodResponse();

        try {
            // 🔹 Debug log (raw response print)
            System.out.println("🔍 Gemini Raw Response: " + rawText);

            // 🔹 Agar Gemini ne extra text bhej diya, sirf { ... } part nikal lo
            int start = rawText.indexOf("{");
            int end = rawText.lastIndexOf("}");
            if (start != -1 && end != -1 && end > start) {
                rawText = rawText.substring(start, end + 1);
            }

            // ✅ Parse JSON
            JSONObject json = new JSONObject(rawText);

            // 🔹 Basic info
            response.setFoodName(json.optString("foodName", "Unknown Food"));
            response.setHealthInsights(json.optString("healthInsights", "No insights"));
            response.setProcessingLevel(json.optString("processingLevel", "Unknown"));
            response.setCulturalOrigin(json.optString("culturalOrigin", "Unknown"));
            response.setIngredientBreakdown(json.optString("ingredientBreakdown", "Unknown"));

            // 🔹 Nutrients
            JSONObject nutrientsJson = json.optJSONObject("nutrients");
            FoodResponse.Nutrients nutrients = new FoodResponse.Nutrients();
            if (nutrientsJson != null) {
                nutrients.setCalories(nutrientsJson.optString("calories", "0"));
                nutrients.setProtein(nutrientsJson.optString("protein", "0g"));
                nutrients.setFat(nutrientsJson.optString("fat", "0g"));
                nutrients.setCarbs(nutrientsJson.optString("carbs", "0g"));
            }
            response.setNutrients(nutrients);

        } catch (JSONException e) {
            e.printStackTrace();

            // 🔹 fallback values agar parsing fail ho jaye
            response.setFoodName("Unknown Food");
            response.setHealthInsights(rawText); // pura text dal dena

            // ✅ Dummy nutrients
            FoodResponse.Nutrients nutrients = new FoodResponse.Nutrients();
            nutrients.setCalories("0");
            nutrients.setProtein("0g");
            nutrients.setFat("0g");
            nutrients.setCarbs("0g");
            response.setNutrients(nutrients);
        }

        return response;
    }
}
