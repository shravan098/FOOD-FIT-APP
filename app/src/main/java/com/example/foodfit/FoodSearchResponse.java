package com.example.foodfit;

import java.util.List;

public class FoodSearchResponse {
    private List<FoodItem> foods;

    public FoodSearchResponse() { }

    public List<FoodItem> getFoods() { return foods; }
    public void setFoods(List<FoodItem> foods) { this.foods = foods; }
}
