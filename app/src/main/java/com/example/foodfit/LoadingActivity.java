package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    private static final int LOADING_TIME = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        // Delay and move to next page
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LoadingActivity.this, Postlogin.class);
            startActivity(intent);
            finish();
        }, LOADING_TIME);
    }
}