package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class InputActivity extends AppCompatActivity {

    private EditText ageInput, heightInput, weightInput, goalWeightInput;
    private TextView resultText, goalWeightLabel;
    private Spinner genderSpinner;
    private Button saveButton, nextButton, backButton;

    private String goalType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input);

        // UI references
        ageInput = findViewById(R.id.ageInput);
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        goalWeightInput = findViewById(R.id.goalWeightInput);
        goalWeightLabel = findViewById(R.id.goalWeightLabel);
        resultText = findViewById(R.id.resultText);
        genderSpinner = findViewById(R.id.genderSpinner);
        saveButton = findViewById(R.id.saveButton);
        nextButton = findViewById(R.id.nextButton);
        backButton = findViewById(R.id.backButton);

        resultText.setVisibility(View.GONE);

        // Get goal type from PlanSelectionActivity
        goalType = getIntent().getStringExtra("goal");
        if (goalType == null) goalType = "maintain weight";
        goalType = goalType.trim().toLowerCase();

        boolean needsGoalWeight = goalType.equals("lose weight") || goalType.equals("gain weight");
        goalWeightInput.setVisibility(needsGoalWeight ? View.VISIBLE : View.INVISIBLE);
        goalWeightLabel.setVisibility(needsGoalWeight ? View.VISIBLE : View.INVISIBLE);

        // 🔙 Back Button
        backButton.setOnClickListener(v -> {
            SessionTracker.actions.add("Clicked Back button at " + System.currentTimeMillis());
            startActivity(new Intent(InputActivity.this, PlanSelectionActivity.class));
            finish();
        });

        // 💾 Save Button
        saveButton.setOnClickListener(v -> {
            resultText.setVisibility(View.GONE);

            String ageStr = ageInput.getText().toString().trim();
            String heightStr = heightInput.getText().toString().trim();
            String weightStr = weightInput.getText().toString().trim();
            String goalWeightStr = goalWeightInput.getText().toString().trim();
            String gender = genderSpinner.getSelectedItem().toString();

            if (ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() || (needsGoalWeight && goalWeightStr.isEmpty())) {
                resultText.setText("Please fill in all fields.");
                resultText.setVisibility(View.VISIBLE);
                return;
            }

            try {
                float age = Float.parseFloat(ageStr);
                float height = Float.parseFloat(heightStr);
                float weight = Float.parseFloat(weightStr);
                float goalWeight = goalWeightStr.isEmpty() ? weight : Float.parseFloat(goalWeightStr);

                if (age < 15 || age > 100) {
                    resultText.setText("Please enter a valid age.");
                    resultText.setVisibility(View.VISIBLE);
                    return;
                }
                if (height < 50 || height > 250) {
                    resultText.setText("Please enter a valid height.");
                    resultText.setVisibility(View.VISIBLE);
                    return;
                }
                if (weight < 20 || weight > 250) {
                    resultText.setText("Please enter a valid weight.");
                    resultText.setVisibility(View.VISIBLE);
                    return;
                }

                if (goalType.equals("lose weight") && goalWeight >= weight) {
                    resultText.setText("Goal weight must be less than current weight because you have selected Lose weight option.");
                    resultText.setVisibility(View.VISIBLE);
                    return;
                }

                if (goalType.equals("gain weight") && goalWeight <= weight) {
                    resultText.setText("Goal weight must be greater than current weight because you have selected Gain weight option.");
                    resultText.setVisibility(View.VISIBLE);
                    return;
                }

                // BMR calculation
                float bmr = gender.equalsIgnoreCase("male") ?
                        (10 * weight) + (6.25f * height) - (5 * age) + 5 :
                        (10 * weight) + (6.25f * height) - (5 * age) - 161;

                float targetCalories;
                String goalMessage;

                switch (goalType) {
                    case "lose weight":
                        targetCalories = bmr * 0.8f;
                        goalMessage = "To lose weight, consume around ";
                        break;
                    case "gain weight":
                        targetCalories = bmr * 1.2f;
                        goalMessage = "To gain weight, consume around ";
                        break;
                    default:
                        targetCalories = bmr;
                        goalMessage = "To maintain your current weight, consume around ";
                        break;
                }

                String resultString = goalMessage + Math.round(targetCalories) + " calories/day.";
                resultText.setText(resultString);
                resultText.setVisibility(View.VISIBLE);

                // 🔥 Firestore logging
                SessionTracker.actions.add("Saved at " + System.currentTimeMillis());
                SessionTracker.actions.add("Gender: " + gender);
                SessionTracker.actions.add("Age: " + age);
                SessionTracker.actions.add("Height: " + height);
                SessionTracker.actions.add("Weight: " + weight);
                SessionTracker.actions.add("Goal Weight: " + goalWeight);
                SessionTracker.actions.add("Goal Type: " + goalType);
                SessionTracker.actions.add("Recommended Intake: " + Math.round(targetCalories) + " kcal");

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("actions", new ArrayList<>(SessionTracker.actions));
                sessionData.put("timestamp", System.currentTimeMillis());

                db.collection("userSessions")
                        .add(sessionData)
                        .addOnSuccessListener(doc -> Log.d("Firestore", "Session saved with ID: " + doc.getId()))
                        .addOnFailureListener(e -> Log.w("Firestore", "Error saving session", e));

            } catch (NumberFormatException e) {
                resultText.setText("Invalid number entered. Please check your inputs.");
                resultText.setVisibility(View.VISIBLE);
            }
        });

        // ⏭️ Next Button → SignUpActivity with all data
        nextButton.setOnClickListener(v -> {
            String age = ageInput.getText().toString().trim();
            String height = heightInput.getText().toString().trim();
            String weight = weightInput.getText().toString().trim();
            String goalWeight = goalWeightInput.getText().toString().trim();
            String gender = genderSpinner.getSelectedItem().toString();
            String bmrResult = resultText.getText().toString();

            Intent intent = new Intent(InputActivity.this, SignUpActivity.class);
            intent.putExtra("age", age);
            intent.putExtra("height", height);
            intent.putExtra("weight", weight);
            intent.putExtra("goalWeight", goalWeight);
            intent.putExtra("gender", gender);
            intent.putExtra("goalType", goalType);
            intent.putExtra("bmrResult", bmrResult);

            SessionTracker.actions.add("Clicked Next button at " + System.currentTimeMillis());

            startActivity(intent);
            finish();
        });
    }
}
