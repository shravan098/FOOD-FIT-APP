package com.example.foodfit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PreviewActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private Button btnCrop, btnDone, btnCancel;

    private Bitmap finalBitmap = null;
    private Uri originalUri = null;
    private String mealType;

    private ActivityResultLauncher<Intent> cropLauncher;
    private ProgressDialog progressDialog;

    // API configuration
    private final String GEMINI_API_KEY = "";
    private final String GEMINI_URL = "";

    // Retry configuration
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_BACKOFF_MS = 1000; // 1 second
    private int retryCount = 0;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        imagePreview = findViewById(R.id.imagePreview);
        btnCrop = findViewById(R.id.btnCrop);
        btnDone = findViewById(R.id.btnDone);
        btnCancel = findViewById(R.id.btnCancel);

        queue = Volley.newRequestQueue(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Analyzing food...");
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            if (queue != null) {
                queue.cancelAll(this);
            }
            progressDialog.dismiss();
        });

        mealType = getIntent().getStringExtra("mealType");
        if (mealType == null || mealType.trim().isEmpty()) mealType = "Unknown";

        boolean fromCamera = getIntent().getBooleanExtra("fromCamera", false);
        if (fromCamera) {
            Bitmap bitmap = getIntent().getParcelableExtra("cameraBitmap");
            if (bitmap != null) {
                finalBitmap = scaleBitmap(bitmap, 1024);
                bitmap.recycle();
                originalUri = saveBitmapToCache(finalBitmap);
                imagePreview.setImageBitmap(finalBitmap);
            }
        } else {
            String uriString = getIntent().getStringExtra("imageUri");
            if (uriString != null) {
                try {
                    originalUri = Uri.parse(uriString);
                    finalBitmap = decodeSampledBitmapFromUri(originalUri, 1024, 1024);
                    imagePreview.setImageBitmap(finalBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }

        cropLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent dataResult = result.getData();
                    if (result.getResultCode() == RESULT_OK && dataResult != null) {
                        Uri resultUri = UCrop.getOutput(dataResult);
                        if (resultUri != null) {
                            try (InputStream inputStream = getContentResolver().openInputStream(resultUri)) {
                                if (inputStream != null) {
                                    finalBitmap = BitmapFactory.decodeStream(inputStream);
                                    imagePreview.setImageBitmap(finalBitmap);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        btnCrop.setOnClickListener(v -> {
            if (originalUri != null) {
                Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped.jpg"));
                Intent cropIntent = UCrop.of(originalUri, destinationUri)
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(800, 800)
                        .getIntent(this);
                cropLauncher.launch(cropIntent);
            }
        });

        btnCancel.setOnClickListener(v -> finish());

        btnDone.setOnClickListener(v -> {
            if (finalBitmap != null) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendImageToGemini(convertBitmapToBase64(finalBitmap), mealType);
            } else {
                Toast.makeText(this, "No image to analyze", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (finalBitmap != null && !finalBitmap.isRecycled()) {
            finalBitmap.recycle();
            finalBitmap = null;
        }
        if (queue != null) {
            queue.cancelAll(this);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxSize) {
        float ratio = Math.min(
                (float) maxSize / bitmap.getWidth(),
                (float) maxSize / bitmap.getHeight()
        );
        int width = Math.round(ratio * bitmap.getWidth());
        int height = Math.round(ratio * bitmap.getHeight());

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;
            try (InputStream stream = getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(stream, null, options);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Uri saveBitmapToCache(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "temp_image.jpg");
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            }
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP);
    }

    private void sendImageToGemini(String base64Image, String mealType) {
        progressDialog.show();
        retryCount = 0;
        makeGeminiRequest(base64Image, mealType);
    }

    private void makeGeminiRequest(String base64Image, String mealType) {
        try {
            JSONObject requestBody = new JSONObject();
            JSONObject imagePart = new JSONObject();
            imagePart.put("inline_data", new JSONObject()
                    .put("mime_type", "image/jpeg")
                    .put("data", base64Image));

            JSONObject textPart = new JSONObject();
            textPart.put("text",
                    "Analyze the food in this image/text. " +
                            "Return ONLY a JSON array. Each element MUST include these keys: " +
                            "foodLabel, calories, protein, fat, carbs, sugar, fiber, sodium, cholesterol, " +
                            "saturatedFat, transFat, addedSugars, vitamins, minerals, glycemicIndex, " +
                            "allergens, verdict, ingredients, servingSize. " +
                            "Values must be numbers (double) where applicable, strings otherwise. " +
                            "Do not include markdown formatting or ```json fences.");

            requestBody.put("contents", new org.json.JSONArray()
                    .put(new JSONObject()
                            .put("parts", new org.json.JSONArray()
                                    .put(imagePart)
                                    .put(textPart))));

            String url = GEMINI_URL + GEMINI_API_KEY;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestBody,
                    response -> {
                        progressDialog.dismiss();
                        try {
                            String textResponse = response
                                    .getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");

                            Intent intent = new Intent(PreviewActivity.this, LoadingActivity2.class);
                            intent.putExtra("mealType", mealType);
                            intent.putExtra("geminiResponse", textResponse.trim());
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error parsing Gemini response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> handleGeminiError(error, base64Image, mealType)
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            logRequestDetails(jsonObjectRequest);

            jsonObjectRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                    30000,
                    0,
                    1.0f
            ));

            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(this, "Error creating Gemini request", Toast.LENGTH_SHORT).show();
        }
    }

    private void logRequestDetails(JsonObjectRequest request) {
        try {
            Log.d("GeminiRequest", "URL: " + request.getUrl());
            Log.d("GeminiRequest", "Method: " + (request.getMethod() == Request.Method.POST ? "POST" : "GET"));
            if (request.getBody() != null) {
                String requestBody = new String(request.getBody(), "UTF-8");
                Log.d("GeminiRequest", "Body: " + requestBody.substring(0, Math.min(200, requestBody.length())) + "...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGeminiError(VolleyError error, String base64Image, String mealType) {
        String errorMessage = "Network error";
        int statusCode = -1;

        if (error.networkResponse != null) {
            statusCode = error.networkResponse.statusCode;
        }

        if (error instanceof TimeoutError) {
            errorMessage = "Request timeout";
        } else if (error instanceof NoConnectionError) {
            errorMessage = "No internet connection";
        } else if (error instanceof AuthFailureError) {
            errorMessage = "Authentication error";
        } else if (error instanceof ServerError) {
            errorMessage = "Server error";
        } else if (error instanceof NetworkError) {
            errorMessage = "Network error";
        } else if (error instanceof ParseError) {
            errorMessage = "Parse error";
        }

        if (statusCode == 429) {
            if (retryCount < MAX_RETRIES) {
                retryCount++;
                long backoffTime = INITIAL_BACKOFF_MS * (long) Math.pow(2, retryCount - 1);

                Toast.makeText(this, "Too many requests. Retrying in " +
                        TimeUnit.MILLISECONDS.toSeconds(backoffTime) + " seconds", Toast.LENGTH_SHORT).show();

                new android.os.Handler().postDelayed(() -> {
                    makeGeminiRequest(base64Image, mealType);
                }, backoffTime);
                return;
            } else {
                errorMessage = "Too many requests. Please wait a moment and try again.";
            }
        } else if (statusCode == 400) {
            errorMessage = "Invalid request. Please check your input.";
        } else if (statusCode == 404) {
            errorMessage = "Service not found. Please try again later.";
        } else if (statusCode >= 500) {
            errorMessage = "Server error. Please try again later.";
        } else if (statusCode == 401 || statusCode == 403) {
            errorMessage = "Authentication failed. Please check your API key.";
        }

        progressDialog.dismiss();
        Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
    }
}
