package com.android.inputmethod.latin.settings;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MenuItem;

import androidx.core.app.ActivityCompat;

import com.android.inputmethod.latin.permissions.PermissionsManager;
import com.android.inputmethod.latin.utils.FragmentUtils;
import com.android.inputmethod.latin.utils.StatsUtils;

public final class SettingsActivity extends PreferenceActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "SettingsActivity";
    private static final String DEFAULT_FRAGMENT = SettingsFragment.class.getName();

    public static final String EXTRA_SHOW_HOME_AS_UP = "show_home_as_up";
    public static final String EXTRA_ENTRY_KEY = "entry";
    public static final String EXTRA_ENTRY_VALUE_LONG_PRESS_COMMA = "long_press_comma";
    public static final String EXTRA_ENTRY_VALUE_APP_ICON = "app_icon";
    public static final String EXTRA_ENTRY_VALUE_NOTICE_DIALOG = "important_notice";
    public static final String EXTRA_ENTRY_VALUE_SYSTEM_SETTINGS = "system_settings";

    private boolean mShowHomeAsUp;

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        Log.d(TAG, "onCreate: ");
        final ActionBar actionBar = getActionBar();
        final Intent intent = getIntent();
        if (actionBar != null) {
            mShowHomeAsUp = intent.getBooleanExtra(EXTRA_SHOW_HOME_AS_UP, true);
            actionBar.setDisplayHomeAsUpEnabled(mShowHomeAsUp);
            actionBar.setHomeButtonEnabled(mShowHomeAsUp);
        }
        StatsUtils.onSettingsActivity(
                intent.hasExtra(EXTRA_ENTRY_KEY) ? intent.getStringExtra(EXTRA_ENTRY_KEY)
                        : EXTRA_ENTRY_VALUE_SYSTEM_SETTINGS);

    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (mShowHomeAsUp && item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public synchronized   Intent getIntent() {
        final Intent intent = super.getIntent();
        final String fragment = intent.getStringExtra(EXTRA_SHOW_FRAGMENT);
        if (fragment == null) {
            intent.putExtra(EXTRA_SHOW_FRAGMENT, DEFAULT_FRAGMENT);
        }
        intent.putExtra(EXTRA_NO_HEADERS, true);
        return intent;
    }

    @Override
    public boolean isValidFragment(final String fragmentName) {
        return FragmentUtils.isValidFragment(fragmentName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionsManager.get(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
