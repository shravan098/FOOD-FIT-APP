package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodSearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private RecyclerView foodRecycler;
    private TextView placeholderMessage;
    private static final String API_KEY = "FiKfVXO2OQUTg8YrXgzIo9URqOJ4d20neLaG7Xds";
    private static final String TAG = "FoodSearch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_search);

        searchInput = findViewById(R.id.searchInput);
        foodRecycler = findViewById(R.id.foodRecycler);
        placeholderMessage = findViewById(R.id.placeholderMessage);

        foodRecycler.setLayoutManager(new LinearLayoutManager(this));
        foodRecycler.setDescendantFocusability(RecyclerView.FOCUS_BLOCK_DESCENDANTS);

        FoodAdapter adapter = new FoodAdapter(this::launchNutrientScreen);
        foodRecycler.setAdapter(adapter);

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchInput.getText().toString().trim();
                if (!query.isEmpty()) {
                    adapter.setData(new ArrayList<>());
                    placeholderMessage.setText("Searching for \"" + query + "\"...");
                    hideKeyboard();
                    searchFood(query, adapter);
                }
                return true;
            }
            return false;
        });
    }

    private void searchFood(String query, FoodAdapter adapter) {
        RetrofitClient.getUSDAService().searchFoods(query, 25, API_KEY)
                .enqueue(new Callback<FoodSearchResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<FoodSearchResponse> call, @NonNull Response<FoodSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getFoods() != null) {
                            List<FoodItem> items = response.body().getFoods();
                            Log.d(TAG, "API returned " + items.size() + " items");
                            adapter.setData(items);

                            if (items.isEmpty()) {
                                placeholderMessage.setText("No results for \"" + query + "\" ❌");
                            } else {
                                placeholderMessage.setText("Suggestions for \"" + query + "\":");
                            }
                        } else {
                            placeholderMessage.setText("No food found ❌");
                            Log.e(TAG, "Response unsuccessful: " + response.code());
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("foodDescription", item.getDescription());
        intent.putExtra("nutrients", new Gson().toJson(item.getFoodNutrients()));

        // ✅ Forward mealType properly
        String mealType = getIntent().getStringExtra("mealType");
        if (mealType != null) {
            intent.putExtra("mealType", mealType);
        }

        startActivity(intent);
    }


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
    }
}
