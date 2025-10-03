package com.example.foodfit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private Context context;
    private List<DocumentSnapshot> mealList;
    private OnMealActionListener listener;

    public interface OnMealActionListener {
        void onDelete(DocumentSnapshot doc);
    }

    public MealAdapter(Context context, List<DocumentSnapshot> mealList, OnMealActionListener listener) {
        this.context = context;
        this.mealList = mealList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.meal_item, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        DocumentSnapshot doc = mealList.get(position);

        String foodName = doc.getString("foodName");

        // Safely read numbers and convert to String
        Object calObj = doc.get("calories");
        String calories = (calObj != null) ? calObj.toString() : "0";

        Object proteinObj = doc.get("protein");
        String protein = (proteinObj != null) ? proteinObj.toString() : "0";

        Object carbsObj = doc.get("carbs");
        String carbs = (carbsObj != null) ? carbsObj.toString() : "0";

        Object fatObj = doc.get("fat");
        String fat = (fatObj != null) ? fatObj.toString() : "0";

        holder.tvFoodName.setText("🍽 " + foodName);
        holder.tvCalories.setText("Calories: " + calories + " kcal\n💪 Protein: " + protein + " g\n🍞 Carbs: " + carbs + " g\n🥑 Fat: " + fat + " g");

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(doc));
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvCalories;
        Button btnDelete;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvCalories = itemView.findViewById(R.id.tvCalories);

            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
