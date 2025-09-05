package com.example.foodfit;

import java.util.List;

public class USDAFoodSearchResponse {
    private final List<USDAFoodItem> foods;

    public USDAFoodSearchResponse(List<USDAFoodItem> foods) {
        this.foods = foods;
    }

    public List<USDAFoodItem> getFoods() {
        return foods;
    }
}