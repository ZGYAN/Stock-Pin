package com.stockpin.stockpinapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;


public class Splash_Screen extends AppCompatActivity {

    SharedPreferences settingsPreferences;
    ConstraintLayout splashScreen;

    private static int SPLASH_TIMEOUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash__screen);

        splashScreen = findViewById(R.id.splashScreen);


        settingsPreferences = getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);

        //Set theme
        if (settingsPreferences.getBoolean("isDark", false)) {
            splashScreen.setBackgroundResource(R.color.black);
        } else {
            splashScreen.setBackgroundResource(R.drawable.back);
        }

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