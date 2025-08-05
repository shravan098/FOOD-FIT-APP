package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class InputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input);

        EditText ageInput = findViewById(R.id.ageInput);
        EditText heightInput = findViewById(R.id.heightInput);
        EditText weightInput = findViewById(R.id.weightInput);
        EditText goalWeightInput = findViewById(R.id.goalWeightInput);
        Spinner genderSpinner = findViewById(R.id.genderSpinner);
        Button nextButton = findViewById(R.id.nextButton);

        String goalType = getIntent().getStringExtra("goal");
        if (goalType == null) goalType = "maintain weight";
        goalType = goalType.trim().toLowerCase();

        boolean needsGoalWeight = goalType.equals("lose weight") || goalType.equals("gain weight");
        goalWeightInput.setVisibility(needsGoalWeight ? View.VISIBLE : View.INVISIBLE);

        String finalGoalType = goalType;
        nextButton.setOnClickListener(v -> {
            String ageStr = ageInput.getText().toString().trim();
            String heightStr = heightInput.getText().toString().trim();
            String weightStr = weightInput.getText().toString().trim();
            String goalWeightStr = goalWeightInput.getText().toString().trim();
            String gender = genderSpinner.getSelectedItem().toString();

            Intent intent = new Intent(InputActivity.this, SignUpActivity.class);
            intent.putExtra("goal", finalGoalType);
            intent.putExtra("age", ageStr);
            intent.putExtra("height", heightStr);
            intent.putExtra("weight", weightStr);
            intent.putExtra("goalWeight", goalWeightStr);
            intent.putExtra("gender", gender);
            startActivity(intent);
            finish();
        });
    }
}