package com.example.foodfit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Lunchmealactivity extends AppCompatActivity {

    private Button btnSearchFood, btnScanFood;
    private RecyclerView recyclerMeals;

    // 📷 Camera & Gallery
    private static final int REQUEST_CAMERA = 101;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> galleryPickerLauncher;

    // 🔥 Firestore
    private FirebaseFirestore db;
    private String userId;
    private MealAdapter adapter;
    private List<DocumentSnapshot> mealList = new ArrayList<>();

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

        adapter = new MealAdapter(this, mealList, new MealAdapter.OnMealActionListener() {
            @Override
            public void onEdit(DocumentSnapshot doc) {
                // 👉 send to GeminiFinalResultActivity for editing
                Intent intent = new Intent(Lunchmealactivity.this, GeminiFinalResultActivity.class);
                intent.putExtra("docId", doc.getId());
                intent.putExtra("foodName", doc.getString("foodName"));
                intent.putExtra("calories", doc.getString("calories"));
                intent.putExtra("protein", doc.getString("protein"));
                intent.putExtra("fat", doc.getString("fat"));
                intent.putExtra("carbs", doc.getString("carbs"));
                intent.putExtra("grams", doc.getDouble("grams"));
                intent.putExtra("mealType", "Lunch"); // ✅ Important
                startActivity(intent);
            }

            @Override
            public void onDelete(DocumentSnapshot doc) {
                doc.getReference().delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(Lunchmealactivity.this, "🗑 Deleted", Toast.LENGTH_SHORT).show();
                            loadMeals();
                        });
            }
        });

        recyclerMeals.setAdapter(adapter);

        btnSearchFood.setOnClickListener(v -> {
            Intent intent = new Intent(Lunchmealactivity.this, FoodSearchActivity.class);
            intent.putExtra("mealType", "Lunch"); // ✅ Pass meal type
            startActivity(intent);
        });

        initLaunchers();
        btnScanFood.setOnClickListener(v -> showScanOptions());

        loadMeals();
    }

    private void loadMeals() {
        db.collection("users")
                .document(userId)
                .collection("meals")
                .whereEqualTo("mealType", "Lunch") // ✅ Filter for Lunch
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mealList.clear();
                    mealList.addAll(queryDocumentSnapshots.getDocuments());
                    adapter.notifyDataSetChanged();

                    // ✅ Calculate total lunch calories
                    int totalCalories = 0;
                    for (DocumentSnapshot doc : mealList) {
                        String calStr = doc.getString("calories");
                        if (calStr != null && !calStr.isEmpty()) {
                            try {
                                totalCalories += Integer.parseInt(calStr);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Save in SharedPreferences
                    getSharedPreferences("FoodFitPrefs", MODE_PRIVATE)
                            .edit()
                            .putInt("lunchConsumed", totalCalories)
                            .apply();
                });
    }

    private void initLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) launchCameraIntent();
                    else Toast.makeText(this, "⚠️ Camera permission denied", Toast.LENGTH_SHORT).show();
                }
        );

        galleryPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        Intent intent = new Intent(Lunchmealactivity.this, PreviewActivity.class);
                        intent.putExtra("fromCamera", false);
                        intent.putExtra("imageUri", uri.toString());
                        intent.putExtra("mealType", "Lunch"); // ✅ Pass meal type
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "⚠️ No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showScanOptions() {
        String[] options = {"📷 Camera", "🖼 Gallery", "❌ Cancel"};

        new AlertDialog.Builder(this)
                .setTitle("Select Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            launchCameraIntent();
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    } else if (which == 1) {
                        galleryPickerLauncher.launch(
                                new PickVisualMediaRequest.Builder()
                                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                        .build()
                        );
                    } else {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void launchCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && requestCode == REQUEST_CAMERA) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Intent intent = new Intent(Lunchmealactivity.this, PreviewActivity.class);
            intent.putExtra("fromCamera", true);
            intent.putExtra("cameraBitmap", photo);
            intent.putExtra("mealType", "Lunch"); // ✅ Pass meal type
            startActivity(intent);
        }
    }
}
