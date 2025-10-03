package com.example.foodfit;

import android.os.Parcel;
import android.os.Parcelable;

public class FoodNutrition implements Parcelable {
    private String foodLabel;
    private double calories, protein, fat, carbs;

    private double sugar, fiber, sodium, cholesterol, saturatedFat, transFat, addedSugars;
    private String vitamins, minerals, allergens;
    private String glycemicIndex, verdict;

    private String ingredients;
    private String servingSize;

    public FoodNutrition(String foodLabel, double calories, double protein, double fat, double carbs,
                         double sugar, double fiber, double sodium, double cholesterol,
                         double saturatedFat, double transFat, double addedSugars,
                         String vitamins, String minerals, String glycemicIndex, String allergens,
                         String verdict, String ingredients, String servingSize) {
        this.foodLabel = foodLabel;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.sugar = sugar;
        this.fiber = fiber;
        this.sodium = sodium;
        this.cholesterol = cholesterol;
        this.saturatedFat = saturatedFat;
        this.transFat = transFat;
        this.addedSugars = addedSugars;
        this.vitamins = vitamins;
        this.minerals = minerals;
        this.glycemicIndex = glycemicIndex;
        this.allergens = allergens;
        this.verdict = verdict;
        this.ingredients = ingredients;
        this.servingSize = servingSize;
    }

    protected FoodNutrition(Parcel in) {
        foodLabel = in.readString();
        calories = in.readDouble();
        protein = in.readDouble();
        fat = in.readDouble();
        carbs = in.readDouble();
        sugar = in.readDouble();
        fiber = in.readDouble();
        sodium = in.readDouble();
        cholesterol = in.readDouble();
        saturatedFat = in.readDouble();
        transFat = in.readDouble();
        addedSugars = in.readDouble();
        vitamins = in.readString();
        minerals = in.readString();
        glycemicIndex = in.readString();
        allergens = in.readString();
        verdict = in.readString();
        ingredients = in.readString();
        servingSize = in.readString();
    }

    public static final Creator<FoodNutrition> CREATOR = new Creator<FoodNutrition>() {
        @Override
        public FoodNutrition createFromParcel(Parcel in) { return new FoodNutrition(in); }
        @Override
        public FoodNutrition[] newArray(int size) { return new FoodNutrition[size]; }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(foodLabel);
        parcel.writeDouble(calories);
        parcel.writeDouble(protein);
        parcel.writeDouble(fat);
        parcel.writeDouble(carbs);
        parcel.writeDouble(sugar);
        parcel.writeDouble(fiber);
        parcel.writeDouble(sodium);
        parcel.writeDouble(cholesterol);
        parcel.writeDouble(saturatedFat);
        parcel.writeDouble(transFat);
        parcel.writeDouble(addedSugars);
        parcel.writeString(vitamins);
        parcel.writeString(minerals);
        parcel.writeString(glycemicIndex);
        parcel.writeString(allergens);
        parcel.writeString(verdict);
        parcel.writeString(ingredients);
        parcel.writeString(servingSize);
    }

    @Override
    public String toString() {
        return foodLabel + "\nCalories: " + calories + " kcal\nProtein: " + protein + " g\n" +
                "Carbs: " + carbs + " g\nFat: " + fat + " g\nSugar: " + sugar + " g\nFiber: " + fiber + " g\n" +
                "Sodium: " + sodium + " mg\nCholesterol: " + cholesterol + " mg\n" +
                "Sat. Fat: " + saturatedFat + " g\nTrans Fat: " + transFat + " g\nAdded Sugars: " + addedSugars + " g\n" +
                "Vitamins: " + vitamins + "\nMinerals: " + minerals + "\n" +
                "Glycemic Index: " + glycemicIndex + "\nAllergens: " + allergens + "\n" +
                "Ingredients: " + ingredients + "\nServing Size: " + servingSize + "\n" +
                "⚠️ Verdict: " + verdict + "\n";
    }

    // 👉 Add Getters
    public String getFoodLabel() { return foodLabel; }
    public double getCalories() { return calories; }
    public double getProtein() { return protein; }
    public double getFat() { return fat; }
    public double getCarbs() { return carbs; }
    public double getSugar() { return sugar; }
    public double getFiber() { return fiber; }
    public double getSodium() { return sodium; }
    public double getCholesterol() { return cholesterol; }
    public double getSaturatedFat() { return saturatedFat; }
    public double getTransFat() { return transFat; }
    public double getAddedSugars() { return addedSugars; }
    public String getVitamins() { return vitamins; }
    public String getMinerals() { return minerals; }
    public String getGlycemicIndex() { return glycemicIndex; }
    public String getAllergens() { return allergens; }
    public String getVerdict() { return verdict; }
    public String getIngredients() { return ingredients; }
    public String getServingSize() { return servingSize; }
}
