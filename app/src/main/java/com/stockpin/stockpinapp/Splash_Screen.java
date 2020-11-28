package com.stockpin.stockpinapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class Splash_Screen extends AppCompatActivity {

    private static int SPLASH_TIMEOUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash__screen);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent homeIntent = new Intent(Splash_Screen.this, MainActivity.class);
                    startActivity(new Intent(homeIntent));
                    finish();
                }
            }, SPLASH_TIMEOUT);

    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }
}