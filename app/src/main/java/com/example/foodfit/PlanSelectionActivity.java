package com.example.foodfit; // ✅ Make sure this matches your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PlanSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_selection);

        // 🎯 Connect buttons
        Button loseWeightBtn = findViewById(R.id.loseWeightButton);
        Button maintainWeightBtn = findViewById(R.id.maintainWeightButton);
        Button gainWeightBtn = findViewById(R.id.gainWeightButton);

        // 🚀 Handle button clicks
        loseWeightBtn.setOnClickListener(view -> openNextScreen("Lose Weight"));
        maintainWeightBtn.setOnClickListener(view -> openNextScreen("Maintain Weight"));
        gainWeightBtn.setOnClickListener(view -> openNextScreen("Gain Weight"));
    }

    // 🧭 Function to navigate forward with selected goal and store to Firebase
    private void openNextScreen(String goalType) {
        // 📝 Firebase write
        DatabaseReference planRef = FirebaseDatabase.getInstance().getReference("userPlan");
        planRef.setValue(goalType);

        // ➡ Transition to InputActivity
        Intent intent = new Intent(PlanSelectionActivity.this, InputActivity.class);
        intent.putExtra("goal", goalType);
        startActivity(intent);
        finish();
    }
}