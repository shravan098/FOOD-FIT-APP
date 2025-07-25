package com.example.foodfit;

import java.util.List;

public class FoodSearchResponse {
    private final List<FoodItem> foods;

    public FoodSearchResponse(List<FoodItem> foods) {
        this.foods = foods;
    }

    public List<FoodItem> getFoods() {
        return foods;
    }
}