package com.example.foodfit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FoodSearch";
    private static final String API_KEY = "FiKfVXO2OQUTg8YrXgzIo9URqOJ4d20neLaG7Xds";

    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultView = findViewById(R.id.resultView);
        resultView.setText(getString(R.string.searching_text));


        RetrofitClient.getService().searchFoods("banana", API_KEY)
                .enqueue(new Callback<FoodSearchResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<FoodSearchResponse> call, @NonNull Response<FoodSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<FoodItem> foods = response.body().getFoods();
                            StringBuilder builder = new StringBuilder();

                            for (FoodItem item : foods) {
                                builder.append("🍽️ Food: ").append(item.getDescription()).append("\n");
                                for (Nutrient nutrient : item.getFoodNutrients()) {
                                    builder.append("🔹 ")
                                            .append(nutrient.getNutrientName())
                                            .append(": ")
                                            .append(nutrient.getValue())
                                            .append("\n");
                                }
                                builder.append("\n");
                            }

                            resultView.setText(builder.toString());
                        } else {
                            resultView.setText(getString(R.string.no_data_found));

                            Log.e(TAG, "Response unsuccessful or no data");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FoodSearchResponse> call, @NonNull Throwable t) {
                        resultView.setText(getString(R.string.api_failed));

                        Log.e(TAG, "API call failed: " + t.getMessage());
                    }
                });
    }
}
