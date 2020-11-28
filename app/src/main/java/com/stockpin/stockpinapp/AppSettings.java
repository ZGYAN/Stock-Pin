package com.stockpin.stockpinapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.stockpin.bubble.BubbleService;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

public class AppSettings extends AppCompatActivity {
    ImageButton exitSettings;
    Switch enableBubbles, hideOnFullscreen;
    ImageView blueBubble, redBubble, yellowBubble, greenBubble, purpleBubble, blackBubble;
    Button aboutApp;
    SharedPreferences settingsPreferences;
    Dialog permissionDialog;
    Button enable_btn, disable_btn;
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;

    AdView settingsBanner;
    InterstitialAd settingsTransitionAd;

    NotificationManager notificationManager;
    int permissionInt, colorChoice;
    boolean hideOnFull;
    FrameLayout adFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        exitSettings = findViewById(R.id.exitSettings);
        enableBubbles = findViewById(R.id.enableBubbleSwitch);
        hideOnFullscreen = findViewById(R.id.enableOnFullscreenSwitch);
        aboutApp = findViewById(R.id.about_btn);
        blueBubble = findViewById(R.id.blueChoice);
        redBubble = findViewById(R.id.redChoice);
        yellowBubble = findViewById(R.id.yellowChoice);
        greenBubble = findViewById(R.id.greenChoice);
        purpleBubble = findViewById(R.id.purpleChoice);
        blackBubble = findViewById(R.id.blackChoice);
        adFrame = findViewById(R.id.settingsAdFrame);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        //Load banner ad
        settingsBanner = new AdView(this);
        settingsBanner.setAdUnitId(getString(R.string.testBannerAd));
        adFrame.addView(settingsBanner);
        loadBanner();

        settingsTransitionAd = new InterstitialAd(this);
        settingsTransitionAd.setAdUnitId(getString(R.string.testFullscreenAd));
        settingsTransitionAd.loadAd(new AdRequest.Builder().build());



        settingsTransitionAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                settingsTransitionAd.loadAd(new AdRequest.Builder().build());
                super.onAdClosed();
            }
        });

        loadSettings();

        aboutApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openAbout = new Intent(getApplicationContext(), About.class);
                startActivity(openAbout);
            }
        });

        exitSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(settingsTransitionAd.isLoaded()) {
                    settingsTransitionAd.show();
                }
                finish();
            }
        });

        enableBubbles.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    notificationManager.cancelAll();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext())) {
                        showPermissionDialog();
                    } else {
                        colorChoice = 1;
                        setBubble();
                        hideOnFullscreen.setClickable(true);

                    }
                } else {
                    removeBubble();
                    colorChoice = 0;
                    setBubble();
                    hideOnFullscreen.setChecked(false);
                    hideOnFullscreen.setClickable(false);
                }
            }
        });

        hideOnFullscreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isMyServiceRunning(BubbleService.class)) {
                    try {
                        if (isChecked) {
                            BubbleService.bubbleViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_HIDE_FULLSCREEN);
                        } else {
                            BubbleService.bubbleViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
                        }
                    } catch (Exception e) {}
                }
            }
        });

        blueBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChoice = 1;
                setBubble();
                restartService();
            }
        });
        redBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChoice = 2;
                setBubble();
                restartService();
            }
        });
        yellowBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChoice = 3;
                setBubble();
                restartService();
            }
        });
        greenBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChoice = 4;
                setBubble();
                restartService();
            }
        });
        purpleBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChoice = 5;
                setBubble();
                restartService();
            }
        });
        blackBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChoice = 6;
                setBubble();
                restartService();
            }
        });


    }

    public void setBubble() {
        switch (colorChoice) {

            case 1:
                if(enableBubbles.isChecked()) {
                    blueBubble.setImageResource(R.drawable.bubble_icon);
                    redBubble.setImageResource(R.drawable.red_bubble_icon);
                    yellowBubble.setImageResource(R.drawable.gold_bubble_icon);
                    greenBubble.setImageResource(R.drawable.green_bubble_icon);
                    purpleBubble.setImageResource(R.drawable.purple_bubble_icon);
                    blackBubble.setImageResource(R.drawable.black_bubble_icon);

                    blueBubble.setBackgroundResource(R.drawable.circle_borders);
                    redBubble.setBackgroundResource(R.color.transparent);
                    yellowBubble.setBackgroundResource(R.color.transparent);
                    greenBubble.setBackgroundResource(R.color.transparent);
                    purpleBubble.setBackgroundResource(R.color.transparent);
                    blackBubble.setBackgroundResource(R.color.transparent);
                }
                break;
            case 2:
                if(enableBubbles.isChecked()) {
                    blueBubble.setImageResource(R.drawable.bubble_icon);
                    redBubble.setImageResource(R.drawable.red_bubble_icon);
                    yellowBubble.setImageResource(R.drawable.gold_bubble_icon);
                    greenBubble.setImageResource(R.drawable.green_bubble_icon);
                    purpleBubble.setImageResource(R.drawable.purple_bubble_icon);
                    blackBubble.setImageResource(R.drawable.black_bubble_icon);

                    blueBubble.setBackgroundResource(R.color.transparent);
                    redBubble.setBackgroundResource(R.drawable.circle_borders);
                    yellowBubble.setBackgroundResource(R.color.transparent);
                    greenBubble.setBackgroundResource(R.color.transparent);
                    purpleBubble.setBackgroundResource(R.color.transparent);
                    blackBubble.setBackgroundResource(R.color.transparent);
                }
                break;
            case 3:
                if(enableBubbles.isChecked()) {
                    blueBubble.setImageResource(R.drawable.bubble_icon);
                    redBubble.setImageResource(R.drawable.red_bubble_icon);
                    yellowBubble.setImageResource(R.drawable.gold_bubble_icon);
                    greenBubble.setImageResource(R.drawable.green_bubble_icon);
                    purpleBubble.setImageResource(R.drawable.purple_bubble_icon);
                    blackBubble.setImageResource(R.drawable.black_bubble_icon);

                    blueBubble.setBackgroundResource(R.color.transparent);
                    redBubble.setBackgroundResource(R.color.transparent);
                    yellowBubble.setBackgroundResource(R.drawable.circle_borders);
                    greenBubble.setBackgroundResource(R.color.transparent);
                    purpleBubble.setBackgroundResource(R.color.transparent);
                    blackBubble.setBackgroundResource(R.color.transparent);
                }
                break;
            case 4:
                if(enableBubbles.isChecked()) {
                    blueBubble.setImageResource(R.drawable.bubble_icon);
                    redBubble.setImageResource(R.drawable.red_bubble_icon);
                    yellowBubble.setImageResource(R.drawable.gold_bubble_icon);
                    greenBubble.setImageResource(R.drawable.green_bubble_icon);
                    purpleBubble.setImageResource(R.drawable.purple_bubble_icon);
                    blackBubble.setImageResource(R.drawable.black_bubble_icon);

                    blueBubble.setBackgroundResource(R.color.transparent);
                    redBubble.setBackgroundResource(R.color.transparent);
                    yellowBubble.setBackgroundResource(R.color.transparent);
                    greenBubble.setBackgroundResource(R.drawable.circle_borders);
                    purpleBubble.setBackgroundResource(R.color.transparent);
                    blackBubble.setBackgroundResource(R.color.transparent);
                }
                break;
            case 5:
                if(enableBubbles.isChecked()) {
                    blueBubble.setImageResource(R.drawable.bubble_icon);
                    redBubble.setImageResource(R.drawable.red_bubble_icon);
                    yellowBubble.setImageResource(R.drawable.gold_bubble_icon);
                    greenBubble.setImageResource(R.drawable.green_bubble_icon);
                    purpleBubble.setImageResource(R.drawable.purple_bubble_icon);
                    blackBubble.setImageResource(R.drawable.black_bubble_icon);

                    blueBubble.setBackgroundResource(R.color.transparent);
                    redBubble.setBackgroundResource(R.color.transparent);
                    yellowBubble.setBackgroundResource(R.color.transparent);
                    greenBubble.setBackgroundResource(R.color.transparent);
                    purpleBubble.setBackgroundResource(R.drawable.circle_borders);
                    blackBubble.setBackgroundResource(R.color.transparent);
                }
                break;
            case 6:
                if(enableBubbles.isChecked()) {
                    blueBubble.setImageResource(R.drawable.bubble_icon);
                    redBubble.setImageResource(R.drawable.red_bubble_icon);
                    yellowBubble.setImageResource(R.drawable.gold_bubble_icon);
                    greenBubble.setImageResource(R.drawable.green_bubble_icon);
                    purpleBubble.setImageResource(R.drawable.purple_bubble_icon);
                    blackBubble.setImageResource(R.drawable.black_bubble_icon);

                    blueBubble.setBackgroundResource(R.color.transparent);
                    redBubble.setBackgroundResource(R.color.transparent);
                    yellowBubble.setBackgroundResource(R.color.transparent);
                    greenBubble.setBackgroundResource(R.color.transparent);
                    purpleBubble.setBackgroundResource(R.color.transparent);
                    blackBubble.setBackgroundResource(R.drawable.circle_borders);
                }
                break;
            default:
                blueBubble.setImageResource(R.drawable.grayed_bubble_icon);
                redBubble.setImageResource(R.drawable.grayed_bubble_icon);
                yellowBubble.setImageResource(R.drawable.grayed_bubble_icon);
                greenBubble.setImageResource(R.drawable.grayed_bubble_icon);
                purpleBubble.setImageResource(R.drawable.grayed_bubble_icon);
                blackBubble.setImageResource(R.drawable.grayed_bubble_icon);

                blueBubble.setBackgroundResource(R.color.transparent);
                redBubble.setBackgroundResource(R.color.transparent);
                yellowBubble.setBackgroundResource(R.color.transparent);
                greenBubble.setBackgroundResource(R.color.transparent);
                purpleBubble.setBackgroundResource(R.color.transparent);
                blackBubble.setBackgroundResource(R.color.transparent);
        }
        saveSettings();
    }

    private void removeBubble() {
        if (isMyServiceRunning(BubbleService.class) && BubbleService.bubbleViewManager != null) {
            stopService(new Intent(getApplicationContext(), BubbleService.class));
        }
    }

    private void addNewBubble() {
        if (!isMyServiceRunning(BubbleService.class)) {
            // launch service
            final Intent blowBubble = new Intent(getApplicationContext(), BubbleService.class);
            try {
                ContextCompat.startForegroundService(this, blowBubble);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Unable to create bubble", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void showPermissionDialog() {
        settingsPreferences = this.getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
        permissionDialog = new Dialog(this);
        permissionDialog.setContentView(R.layout.confirmation_dialog);
        enable_btn = permissionDialog.findViewById(R.id.enable_permission);
        disable_btn = permissionDialog.findViewById(R.id.disable_permission);
        permissionDialog.setCancelable(false);
        permissionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        disable_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = settingsPreferences.edit();
                editor.putInt("permission", 0);
                editor.commit();
                enableBubbles.setChecked(false);
                permissionDialog.dismiss();
            }
        });
        enable_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                permissionDialog.dismiss();
            }
        });
        permissionDialog.show();

    }

    public void saveSettings() {
        permissionInt = -1;
        if (enableBubbles.isChecked()) {
            permissionInt = 1;
        } else {
            permissionInt = 0;
        }

        hideOnFull = hideOnFullscreen.isChecked();

        SharedPreferences settingsPreferences = getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsPreferences.edit();
        editor.putInt("permission", permissionInt);
        editor.putInt("bubbleColor", colorChoice);
        editor.putBoolean("hideOnFull", hideOnFull);

        editor.commit();
    }

    public void loadSettings() {
        SharedPreferences settingsPreferences = getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsPreferences.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionInt == 1 && !Settings.canDrawOverlays(AppSettings.this)) {
            editor.putInt("permission", -1);
            editor.commit();
        }

        permissionInt = settingsPreferences.getInt("permission", -1);
        colorChoice = settingsPreferences.getInt("bubbleColor", 1); //1 - 6 (grid layout)
        hideOnFull = settingsPreferences.getBoolean("hideOnFull", false);

        enableBubbles.setChecked(permissionInt == 1);
        setBubble();
        hideOnFullscreen.setChecked(hideOnFull);

    }

    public void restartService() {
        if (isMyServiceRunning(BubbleService.class)) {
            removeBubble();
            addNewBubble();
        }
    }

    private void loadBanner() {
        // Create an ad request. Check your logcat output for the hashed device ID
        // to get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
        // device."
        AdRequest adRequest = new AdRequest.Builder().build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        settingsBanner.setAdSize(adSize);


        // Step 5 - Start loading the ad in the background.
        settingsBanner.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                // You don't have permission
                settingsPreferences = this.getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settingsPreferences.edit();
                editor.putInt("permission", 0);
                editor.putInt("bubbleColor", 0);
                editor.commit();
                //Toast.makeText(getApplicationContext(), "YOU DON'T GOT THE PERMISSION", Toast.LENGTH_SHORT).show();
            } else {
                // gained the permission
                settingsPreferences = this.getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settingsPreferences.edit();
                editor.putInt("permission", 1);
                editor.putInt("bubbleColor", 1);
                editor.commit();

            }
            loadSettings();
        }
    }

    @Override
    protected void onResume() {
        loadSettings();
        super.onResume();
    }

    @Override
    protected void onPause() {
        saveSettings();
        super.onPause();
    }

    @Override
    protected void onStop() {
        saveSettings();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if(settingsTransitionAd.isLoaded()) {
            settingsTransitionAd.show();
        }
        super.onBackPressed();
    }
}