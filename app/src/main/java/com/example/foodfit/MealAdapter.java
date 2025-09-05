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
        void onEdit(DocumentSnapshot doc);
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
        String calories = doc.getString("calories");

        holder.tvFoodName.setText("🍽 " + foodName);
        holder.tvCalories.setText("Calories: " + calories + " kcal");

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(doc));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(doc));
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvCalories;
        Button btnEdit, btnDelete;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
