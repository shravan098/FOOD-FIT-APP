package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodSearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private RecyclerView foodRecycler;
    private static final String API_KEY = "FiKfVXO2OQUTg8YrXgzIo9URqOJ4d20neLaG7Xds";
    private static final String TAG = "FoodSearch";

    @Override
    protected void onResume() {
        super.onResume();
        SessionTracker.actions.add("Entered FoodSearchActivity at " + System.currentTimeMillis());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_search);

        searchInput = findViewById(R.id.searchInput);
        foodRecycler = findViewById(R.id.foodRecycler);
        TextView placeholderMessage = findViewById(R.id.placeholderMessage);

        foodRecycler.setLayoutManager(new LinearLayoutManager(this));
        FoodAdapter adapter = new FoodAdapter(this::launchNutrientScreen);
        foodRecycler.setAdapter(adapter);

        searchInput.setText("");

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            Log.d(TAG, "EditorAction triggered with id: " + actionId);
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchInput.getText().toString().trim();
                Log.d(TAG, "User searched for: " + query);
                if (!query.isEmpty()) {
                    SessionTracker.actions.add("Searched for \"" + query + "\" at " + System.currentTimeMillis());
                    adapter.setData(List.of());
                    placeholderMessage.setText("Searching for \"" + query + "\"...");
                    searchFood(query, adapter, placeholderMessage);
                }
                return true;
            }
            return false;
        });
    }

    private void searchFood(String query, FoodAdapter adapter, TextView placeholderMessage) {
        Log.d(TAG, "Making API call for: " + query);
        RetrofitClient.getService().searchFoods(query, API_KEY).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<FoodSearchResponse> call, @NonNull Response<FoodSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FoodItem> items = response.body().getFoods();
                    Log.d(TAG, "API response size: " + items.size());
                    for (FoodItem item : items) {
                        Log.d(TAG, "Food: " + item.getDescription());
                    }
                    if (items.isEmpty()) {
                        placeholderMessage.setText("No results for \"" + query + "\" ❌");
                    } else {
                        placeholderMessage.setText("Suggestions for \"" + query + "\":");
                    }
                    adapter.setData(items);
                } else {
                    placeholderMessage.setText("No food found ❌");
                    Log.e(TAG, "Response unsuccessful or empty");
                }
            }

            @Override
            public void onFailure(@NonNull Call<FoodSearchResponse> call, @NonNull Throwable t) {
                placeholderMessage.setText("API error: " + t.getMessage());
                Log.e(TAG, "API failed: " + t.getMessage());
            }
        });
    }

    private void launchNutrientScreen(FoodItem item) {
        SessionTracker.actions.add("Selected food item: " + item.getDescription() + " at " + System.currentTimeMillis());
        Log.d(TAG, "Launching MainActivity with: " + item.getDescription());
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("foodDescription", item.getDescription());
        intent.putExtra("nutrients", new Gson().toJson(item.getFoodNutrients()));
        startActivity(intent);
    }
}