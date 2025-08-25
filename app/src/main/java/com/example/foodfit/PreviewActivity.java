package com.example.foodfit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class PreviewActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private Button btnCrop, btnDone, btnCancel;

    private Bitmap finalBitmap = null;
    private Uri originalUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        imagePreview = findViewById(R.id.imagePreview);
        btnCrop = findViewById(R.id.btnCrop);
        btnDone = findViewById(R.id.btnDone);
        btnCancel = findViewById(R.id.btnCancel);

        Intent intent = getIntent();
        boolean fromCamera = intent.getBooleanExtra("fromCamera", false);

        if (fromCamera) {
            Bitmap bitmap = intent.getParcelableExtra("cameraBitmap");
            if (bitmap != null) {
                finalBitmap = bitmap;
                originalUri = getImageUri(bitmap);
                imagePreview.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "❌ Failed to load camera image", Toast.LENGTH_SHORT).show();
            }
        } else {
            String uriString = intent.getStringExtra("imageUri");
            if (uriString != null) {
                try {
                    originalUri = Uri.parse(uriString);
                    InputStream inputStream = getContentResolver().openInputStream(originalUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    finalBitmap = bitmap;
                    imagePreview.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "❌ Failed to load gallery image", Toast.LENGTH_SHORT).show();
                }
            }
        }

        btnCrop.setOnClickListener(v -> {
            if (originalUri != null) {
                Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped.jpg"));
                UCrop.of(originalUri, destinationUri)
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(800, 800)
                        .start(PreviewActivity.this);
            } else {
                Toast.makeText(this, "⚠️ No image to crop", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> finish());

        btnDone.setOnClickListener(v -> {
            if (finalBitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();

                Toast.makeText(this, "📤 Sending image to server...", Toast.LENGTH_SHORT).show();

                // TODO: Replace with actual API call or loading screen
                Intent resultIntent = new Intent();
                resultIntent.putExtra("imageBytes", imageBytes);
                startActivity(resultIntent);
            } else {
                Toast.makeText(this, "⚠️ No image to process", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Convert Bitmap to Uri for cropping
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "TempImage", null);
        return Uri.parse(path);
    }

    // Handle cropped image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri resultUri = UCrop.getOutput(data);
            try {
                InputStream inputStream = getContentResolver().openInputStream(resultUri);
                Bitmap croppedBitmap = BitmapFactory.decodeStream(inputStream);
                finalBitmap = croppedBitmap;
                imagePreview.setImageBitmap(croppedBitmap);
                Toast.makeText(this, "✅ Image cropped!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "❌ Failed to load cropped image", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "⚠️ Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}