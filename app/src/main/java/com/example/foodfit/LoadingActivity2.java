package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity2 extends AppCompatActivity {

    private ProgressBar progressBar;
    private static final String API_KEY = "AIzaSyB5J_sDCtUnpBa8tDbn1cMu9bQy4y1WOwY"; // 👈 API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadingg);

        progressBar = findViewById(R.id.progressBar);

        String base64Image = getIntent().getStringExtra("base64Image");
        if (base64Image != null) {
            sendImageToGemini(base64Image);
        } else {
            Toast.makeText(this, "❌ No image data found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendImageToGemini(String base64Image) {
        GeminiApi api = RetrofitClient.getGeminiService();
        FoodRequest request = new FoodRequest(base64Image);

        api.analyzeFood(API_KEY, request).enqueue(new Callback<GeminiRawResponse>() {
            @Override
            public void onResponse(Call<GeminiRawResponse> call, Response<GeminiRawResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 🔹 Raw Gemini text
                    String rawText = response.body().getText();

                    // 🔹 ParseFoodResponse class ka use karke parse karna
                    FoodResponse result = ParseFoodResponse.fromGeminiRaw(rawText);

                    // 🔹 Next screen me bhejna
                    Intent intent = new Intent(LoadingActivity2.this, ResultActivity.class);
                    intent.putExtra("foodName", result.getFoodName());
                    intent.putExtra("calories", result.getNutrients().getCalories());
                    intent.putExtra("protein", result.getNutrients().getProtein());
                    intent.putExtra("fat", result.getNutrients().getFat());
                    intent.putExtra("carbs", result.getNutrients().getCarbs());
                    intent.putExtra("healthInsights", result.getHealthInsights());
                    intent.putExtra("processingLevel", result.getProcessingLevel());
                    intent.putExtra("culturalOrigin", result.getCulturalOrigin());
                    intent.putExtra("ingredientBreakdown", result.getIngredientBreakdown());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoadingActivity2.this, "❌ Gemini response failed", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<GeminiRawResponse> call, Throwable t) {
                Toast.makeText(LoadingActivity2.this, "⚠️ API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
