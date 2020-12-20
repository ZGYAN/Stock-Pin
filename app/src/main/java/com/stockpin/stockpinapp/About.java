package com.stockpin.stockpinapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
public class About extends AppCompatActivity {

    TextView privacy, terms;
    ImageView about, source;
    ImageButton back, share;
    Vibrator vibrator;
    ConstraintLayout aboutScreen;

    SharedPreferences settingsPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        back = findViewById(R.id.exitAbout);
        about = findViewById(R.id.about_image);
        privacy = findViewById(R.id.privacy);
        source = findViewById(R.id.source);
        terms = findViewById(R.id.terms);
        share = findViewById(R.id.shareButton);
        aboutScreen = findViewById(R.id.aboutScreen);

        settingsPreferences = getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openUrl = new Intent(Intent.ACTION_VIEW);
                openUrl.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
                startActivity(openUrl);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Stock Pin");
                    String shareMessage= "Pin your favorite stocks to your screen with STOCK PIN!!! Download it on Google Play:\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID ;
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Share App"));
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openUrl = new Intent(Intent.ACTION_VIEW);
                openUrl.setData(Uri.parse("https://thefutronicstudios.blogspot.com/p/privacy.html"));
                startActivity(openUrl);
            }
        });

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openUrl = new Intent(Intent.ACTION_VIEW);
                openUrl.setData(Uri.parse("https://thefutronicstudios.blogspot.com/p/terms-and-conditions.html"));
                startActivity(openUrl);
            }
        });


        source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openUrl = new Intent(Intent.ACTION_VIEW);
                openUrl.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
                startActivity(openUrl);
            }
        });

        source.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(About.this, "Developer: Achuna Ofonedu 👨🏿‍💻", Toast.LENGTH_LONG).show();
                vibrator.vibrate(200);
                return false;
            }
        });

    }

}