package com.example.foodfit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class USDAFoodAdapter extends RecyclerView.Adapter<USDAFoodAdapter.FoodViewHolder> {

    private final List<USDAFoodItem> foodList = new ArrayList<>();
    private final OnFoodClickListener clickListener;

    public interface OnFoodClickListener {
        void onFoodClick(USDAFoodItem item);
    }

    public USDAFoodAdapter(OnFoodClickListener listener) {
        this.clickListener = listener;
    }

    public void setData(List<USDAFoodItem> items) {
        foodList.clear();
        foodList.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        USDAFoodItem item = foodList.get(position);
        holder.foodName.setText(item.getDescription());
        holder.itemView.setOnClickListener(v -> clickListener.onFoodClick(item));
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView foodName;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.foodName);
        }
    }
}