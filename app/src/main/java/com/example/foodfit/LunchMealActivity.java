package com.example.foodfit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class LunchMealActivity extends AppCompatActivity {

    private Button btnSearchFood, btnScanFood;
    private RecyclerView recyclerMeals;

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    private FirebaseFirestore db;
    private String userId;
    private MealAdapter adapter;
    private List<DocumentSnapshot> mealList = new ArrayList<>();

    private static final String MEAL_TYPE = "Lunch"; // ✅ Correct meal type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lunchmealactivity);

        btnSearchFood = findViewById(R.id.btnSearchFood);
        btnScanFood = findViewById(R.id.btnScanFood);
        recyclerMeals = findViewById(R.id.recyclerMeals);
        recyclerMeals.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupAdapter();
        recyclerMeals.setAdapter(adapter);

        btnSearchFood.setOnClickListener(v -> {
            Intent intent = new Intent(LunchMealActivity.this, FoodSearchActivity.class);
            intent.putExtra("mealType", MEAL_TYPE);
            startActivity(intent);
        });

        initLaunchers();
        btnScanFood.setOnClickListener(v -> showScanOptions());

        loadMeals();
    }

    private void setupAdapter() {
        adapter = new MealAdapter(this, mealList, new MealAdapter.OnMealActionListener() {
            // Removed @Override to avoid compile error
            public void onEdit(DocumentSnapshot doc) {
                Intent intent = new Intent(LunchMealActivity.this, LunchMealActivity.class);
                intent.putExtra("docId", doc.getId());
                intent.putExtra("foodName", safeGetString(doc, "foodName"));
                intent.putExtra("calories", safeGetString(doc, "calories"));
                intent.putExtra("protein", safeGetString(doc, "protein"));
                intent.putExtra("fat", safeGetString(doc, "fat"));
                intent.putExtra("carbs", safeGetString(doc, "carbs"));
                intent.putExtra("grams", safeGetDouble(doc, "grams"));
                intent.putExtra("mealType", MEAL_TYPE); // ✅ Always passes correct meal type
                startActivity(intent);
            }

            public void onDelete(DocumentSnapshot doc) {
                doc.getReference().delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(LunchMealActivity.this, "🗑 Deleted", Toast.LENGTH_SHORT).show();
                            loadMeals();
                        })
                        .addOnFailureListener(e -> Toast.makeText(LunchMealActivity.this, "❌ Failed to delete", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadMeals() {
        db.collection("users")
                .document(userId)
                .collection("meals")
                .whereEqualTo("mealType", MEAL_TYPE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mealList.clear();
                    mealList.addAll(queryDocumentSnapshots.getDocuments());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "❌ Failed to load meals", Toast.LENGTH_SHORT).show());
    }

    private String safeGetString(DocumentSnapshot doc, String field) {
        Object obj = doc.get(field);
        return obj != null ? obj.toString() : "0";
    }

    private double safeGetDouble(DocumentSnapshot doc, String field) {
        Object obj = doc.get(field);
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try { return Double.parseDouble(obj.toString()); } catch (Exception e) { return 0.0; }
    }

    private void initLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) launchCamera();
                    else Toast.makeText(this, "⚠️ Camera permission denied", Toast.LENGTH_SHORT).show();
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        Intent intent = new Intent(LunchMealActivity.this, PreviewActivity.class);
                        intent.putExtra("fromCamera", true);
                        intent.putExtra("cameraBitmap", photo);
                        intent.putExtra("mealType", MEAL_TYPE); // ✅ Pass correct meal type
                        startActivity(intent);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Intent intent = new Intent(LunchMealActivity.this, PreviewActivity.class);
                        intent.putExtra("fromCamera", false);
                        intent.putExtra("imageUri", uri.toString());
                        intent.putExtra("mealType", MEAL_TYPE); // ✅ Pass correct meal type
                        startActivity(intent);
                    }
                });
    }

    private void showScanOptions() {
        String[] options = {"📷 Camera", "🖼 Gallery", "❌ Cancel"};

        new AlertDialog.Builder(this)
                .setTitle("Select Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                            launchCamera();
                        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                    } else if (which == 1) {
                        galleryLauncher.launch("image/*");
                    } else dialog.dismiss();
                }).show();
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }
}
