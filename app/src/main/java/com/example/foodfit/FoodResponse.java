package com.example.foodfit;

public class FoodResponse {
    private String foodName;
    private Nutrients nutrients;
    private String healthInsights;
    private String processingLevel;
    private String culturalOrigin;
    private String ingredientBreakdown;

    // --- Getters ---
    public String getFoodName() {
        return foodName;
    }

    public Nutrients getNutrients() {
        return nutrients;
    }

    public String getHealthInsights() {
        return healthInsights;
    }

    public String getProcessingLevel() {
        return processingLevel;
    }

    public String getCulturalOrigin() {
        return culturalOrigin;
    }

    public String getIngredientBreakdown() {
        return ingredientBreakdown;
    }

    // --- Setters ---
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public void setNutrients(Nutrients nutrients) {
        this.nutrients = nutrients;
    }

    public void setHealthInsights(String healthInsights) {
        this.healthInsights = healthInsights;
    }

    public void setProcessingLevel(String processingLevel) {
        this.processingLevel = processingLevel;
    }

    public void setCulturalOrigin(String culturalOrigin) {
        this.culturalOrigin = culturalOrigin;
    }

    public void setIngredientBreakdown(String ingredientBreakdown) {
        this.ingredientBreakdown = ingredientBreakdown;
    }

    // --- Nutrients inner class ---
    public static class Nutrients {
        private String calories;
        private String protein;
        private String fat;
        private String carbs;

        // Getters
        public String getCalories() {
            return calories;
        }

        public String getProtein() {
            return protein;
        }

        public String getFat() {
            return fat;
        }

        public String getCarbs() {
            return carbs;
        }

        // Setters
        public void setCalories(String calories) {
            this.calories = calories;
        }

        public void setProtein(String protein) {
            this.protein = protein;
        }

        public void setFat(String fat) {
            this.fat = fat;
        }

        public void setCarbs(String carbs) {
            this.carbs = carbs;
        }
    }
}
