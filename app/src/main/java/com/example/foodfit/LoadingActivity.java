package com.example.foodfit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        int dailyCalorie = getIntent().getIntExtra("dailyCalorie", 1340);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LoadingActivity.this, Postlogin.class);
            intent.putExtra("dailyCalorie", dailyCalorie);
            startActivity(intent);
            finish();
        }, 2000); // 2 sec delay
    }
}
