/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.latin.setup;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.inputmethod.latin.settings.SettingsActivity;
import com.sikderithub.keyboard.R;

// TODO: Use Fragment to implement welcome screen and setup steps.
public class SecondSetupWizardActivity extends AppCompatActivity {

    private static final String TAG = "SetupWizardActivity";
    private static final int INPUT_METHOD_SETTINGS_REQUEST_CODE = 1001;
    private static final String PREF_NAME = "SetupPrefs";
    private static final String FIRST_STEP_KEY = "firstStep";
    private static final String SECOND_STEP_KEY = "secondStep";
    private static final String THIRD_STEP_KEY = "thirdStep";

    private FrameLayout secondSetupWizardLayout;
    private View firstSetupScreen;
    private View secondSetupScreen;
    private View completeSetupPage;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.setup_wizard_new);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        secondSetupWizardLayout = findViewById(R.id.second_setup_wizard);
        firstSetupScreen = findViewById(R.id.second_setup_welcome_screen);
        secondSetupScreen = findViewById(R.id.second_setup_screen_layout);
        completeSetupPage = findViewById(R.id.complete_setup_page);

        boolean firstStep = sharedPreferences.getBoolean(FIRST_STEP_KEY, false);
        boolean secondStep = sharedPreferences.getBoolean(SECOND_STEP_KEY, false);
        boolean thirdStep = sharedPreferences.getBoolean(THIRD_STEP_KEY, false);

        if (firstStep) {
            showSecondSetupScreen();
        } else {
            showFirstSetupScreen();
        }

        if (thirdStep) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        }

        if (!isNotificationPermissionGranted(this)) {
            // Request notification permission
            Log.d(TAG, "onStart: not granted");
            askNotificationPermission();
        }
    }

    private void askNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }
    }

    // Check if the notification permission is granted
    public boolean isNotificationPermissionGranted(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return notificationManager.areNotificationsEnabled();
        }
        return true;
    }

    private void showFirstSetupScreen() {
        secondSetupWizardLayout.removeAllViews();
        secondSetupWizardLayout.addView(firstSetupScreen);
        firstSetupScreen.setVisibility(View.VISIBLE);
        secondSetupScreen.setVisibility(View.GONE);
        completeSetupPage.setVisibility(View.GONE);

        Button activateKeyboardButton = findViewById(R.id.btn_active_keyboard);

        activateKeyboardButton.setOnClickListener(v -> invokeLanguageAndInputSettings());
    }

    private void showSecondSetupScreen() {
        secondSetupWizardLayout.removeAllViews();
        secondSetupWizardLayout.addView(secondSetupScreen);
        firstSetupScreen.setVisibility(View.GONE);
        secondSetupScreen.setVisibility(View.VISIBLE);
        completeSetupPage.setVisibility(View.GONE);

        Button selectKeyboardButton = findViewById(R.id.btn_select_keyboard);

        selectKeyboardButton.setOnClickListener(v -> invokeInputMethodPicker());

        if (sharedPreferences.getBoolean(SECOND_STEP_KEY, false)) {
            showCompleteSetupPage();
        }
    }

    private void showCompleteSetupPage() {
        secondSetupWizardLayout.removeAllViews();
        secondSetupWizardLayout.addView(completeSetupPage);
        firstSetupScreen.setVisibility(View.GONE);
        secondSetupScreen.setVisibility(View.GONE);
        completeSetupPage.setVisibility(View.VISIBLE);

        TextView gotoSetting = findViewById(R.id.goto_setting_button);
        gotoSetting.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            finish();
            sharedPreferences.edit().putBoolean(THIRD_STEP_KEY, true).apply();
        });
    }

    private void invokeLanguageAndInputSettings() {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(intent, INPUT_METHOD_SETTINGS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INPUT_METHOD_SETTINGS_REQUEST_CODE) {
            if (isInputMethodSettingsComplete()) {
                showSecondSetupScreen();
                sharedPreferences.edit().putBoolean(FIRST_STEP_KEY, true).apply();
            } else {
                showFirstSetupScreen();
                sharedPreferences.edit().putBoolean(FIRST_STEP_KEY, false).apply();
            }
        }
    }

    private boolean isInputMethodSettingsComplete() {
        return isKeyboardActivated();
    }

    private boolean isKeyboardActivated() {
        return true;
    }

    private void invokeInputMethodPicker() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showInputMethodPicker();
        showCompleteSetupPage();
        sharedPreferences.edit().putBoolean(SECOND_STEP_KEY, true).apply();
    }
}