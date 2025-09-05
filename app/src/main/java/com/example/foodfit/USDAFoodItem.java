package com.example.foodfit;

import java.util.List;

public class USDAFoodItem {
    private String description;
    private int fdcId;
    private List<USDANutrient> foodNutrients;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getFdcId() { return fdcId; }
    public void setFdcId(int fdcId) { this.fdcId = fdcId; }

    public List<USDANutrient> getFoodNutrients() { return foodNutrients; }
    public void setFoodNutrients(List<USDANutrient> foodNutrients) { this.foodNutrients = foodNutrients; }
}