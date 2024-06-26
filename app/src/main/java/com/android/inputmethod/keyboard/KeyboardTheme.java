/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.inputmethod.keyboard;

import static com.sikderithub.keyboard.R.style.KeyboardTheme_LXX_Light;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.inputmethod.compat.BuildCompatUtils;
import com.sikderithub.keyboard.R;

import java.util.ArrayList;
import java.util.Arrays;

public final class KeyboardTheme implements Comparable<KeyboardTheme> {

    // These should be aligned with Keyboard.themeId and Keyboard.Case.keyboardTheme
    // attributes' values in attrs.xml.
    public static final int THEME_ID_ICS = 0;
    public static final int THEME_ID_KLP = 2;
    public static final int THEME_ID_LXX_LIGHT = 3;
    public static final int THEME_ID_LXX_DARK = 4;
    public static final int THEME_ID_MJ_LIGHT = 5;
    public static final int THEME_ID_CUSTOM = 6;
    public static final int THEME_ID_MJMLight = 7;
    public static final int THEME_ELEGANT_MIDNIGHT_AQUA = 8;
    public static final int THEME_DARK_LIME = 9;
    public static final int THEME_MIDNIGHT_BOLOSSOM = 10;
    public static final int THEME_MYSTIC_MAGENTA = 11;
    public static final int THEME_OCEANIC_ELEGANCE = 12;
    public static final int THEME_PURPLE_HAZE = 13;

    public static final int DEFAULT_THEME_ID = THEME_ID_KLP;
    /* package private for testing */
    public static final KeyboardTheme[] KEYBOARD_THEMES = {
            new KeyboardTheme(THEME_ID_MJ_LIGHT, "MJLight", R.style.KeyboardTheme_custom_mj_light,
                    // Default theme for LXX.
                    VERSION_CODES.LOLLIPOP),

            new KeyboardTheme(THEME_ID_MJMLight, "MJMLight", R.style.KeyboardTheme_MJ_Light,
                    VERSION_CODES.LOLLIPOP),

            new KeyboardTheme(THEME_ELEGANT_MIDNIGHT_AQUA, "Elegant Midnight Aqua", R.style.KeyboardTheme_Elegant_midnight_aqua,
                    // Default theme for LXX.
                    VERSION_CODES.LOLLIPOP),
            new KeyboardTheme(THEME_MIDNIGHT_BOLOSSOM, "Midnight Bolossom", R.style.KeyboardTheme_midnight_bolossom,
                    // Default theme for LXX.
                    VERSION_CODES.LOLLIPOP),
            new KeyboardTheme(THEME_DARK_LIME, "Dark Lime", R.style.KeyboardTheme_dark_lime,
                    // Default theme for LXX.
                    VERSION_CODES.LOLLIPOP),
            new KeyboardTheme(THEME_MYSTIC_MAGENTA, "Mystic Magenta", R.style.KeyboardTheme_mystic_magenta,
                    // Default theme for LXX.
                    VERSION_CODES.LOLLIPOP),
            new KeyboardTheme(THEME_OCEANIC_ELEGANCE, "Oceanic Elegance", R.style.KeyboardTheme_oceanic_elegance,
                    // Default theme for LXX.
                    VERSION_CODES.LOLLIPOP),
            new KeyboardTheme(THEME_PURPLE_HAZE, "Purple Haze", R.style.KeyboardTheme_purple_haze,
                    // Default theme for LXX.
                    VERSION_CODES.LOLLIPOP),

            new KeyboardTheme(THEME_ID_ICS, "ICS", R.style.KeyboardTheme_ICS,
                    // This has never been selected because we support ICS or later.
                    VERSION_CODES.BASE),
            new KeyboardTheme(THEME_ID_KLP, "KLP", R.style.KeyboardTheme_KLP,
                    // Default theme for ICS, JB, and KLP.
                    VERSION_CODES.ICE_CREAM_SANDWICH),
            new KeyboardTheme(THEME_ID_LXX_LIGHT, "LXXLight", KeyboardTheme_LXX_Light,
                    // Default theme for LXX.
                    VERSION_CODES.LOLLIPOP),
            new KeyboardTheme(THEME_ID_LXX_DARK, "LXXDark", R.style.KeyboardTheme_LXX_Dark,
                    // This has never been selected as default theme.
                    VERSION_CODES.BASE),

            new KeyboardTheme(THEME_ID_CUSTOM, "Custom", R.style.KeyboardTheme_CUSTOM,
                    // This has never been selected as default theme.
                    VERSION_CODES.BASE),
    };
    static final String KLP_KEYBOARD_THEME_KEY = "pref_keyboard_layout_20110916";
    static final String LXX_KEYBOARD_THEME_KEY = "pref_keyboard_theme_20140509";
    private static final String CUSTOM_THEME_ID_KEY = "pref_keyboard_theme_custom_id_10002";
    private static final String TAG = KeyboardTheme.class.getSimpleName();
    public static int THEME_TYPE_BUILT_IN = 0;
    public static int THEME_TYPE_CUSTOM = 1;
    public static String THEME_TYPE_KEY = "pref_keyboard_theme_type_10001";
    private static KeyboardTheme[] AVAILABLE_KEYBOARD_THEMES;
    public final int mThemeId;
    public final int mStyleId;
    public final String mThemeName;
    public final int mMinApiVersion;

    // Note: The themeId should be aligned with "themeId" attribute of Keyboard style
    // in values/themes-<style>.xml.
    private KeyboardTheme(final int themeId, final String themeName, final int styleId,
                          final int minApiVersion) {
        mThemeId = themeId;
        mThemeName = themeName;
        mStyleId = styleId;
        mMinApiVersion = minApiVersion;
    }

    /* package private for testing */
    static KeyboardTheme searchKeyboardThemeById(final int themeId,
                                                 final KeyboardTheme[] availableThemeIds) {
        // TODO: This search algorithm isn't optimal if there are many themes.
        for (final KeyboardTheme theme : availableThemeIds) {
            if (theme.mThemeId == themeId) {
                return theme;
            }
        }
        return null;
    }

    /* package private for testing */
    static KeyboardTheme getDefaultKeyboardTheme(final SharedPreferences prefs,
                                                 final int sdkVersion, final KeyboardTheme[] availableThemeArray) {
        final String klpThemeIdString = prefs.getString(KLP_KEYBOARD_THEME_KEY, null);
        if (klpThemeIdString != null) {
            if (sdkVersion <= VERSION_CODES.KITKAT) {
                try {
                    final int themeId = Integer.parseInt(klpThemeIdString);
                    final KeyboardTheme theme = searchKeyboardThemeById(themeId,
                            availableThemeArray);
                    if (theme != null) {
                        return theme;
                    }
                    Log.w(TAG, "Unknown keyboard theme in KLP preference: " + klpThemeIdString);
                } catch (final NumberFormatException e) {
                    Log.w(TAG, "Illegal keyboard theme in KLP preference: " + klpThemeIdString, e);
                }
            }
            // Remove old preference.
            Log.i(TAG, "Remove KLP keyboard theme preference: " + klpThemeIdString);
            prefs.edit().remove(KLP_KEYBOARD_THEME_KEY).apply();
        }
        // TODO: This search algorithm isn't optimal if there are many themes.
        for (final KeyboardTheme theme : availableThemeArray) {
            if (sdkVersion >= theme.mMinApiVersion) {
                return theme;
            }
        }
        return searchKeyboardThemeById(DEFAULT_THEME_ID, availableThemeArray);
    }

    public static String getKeyboardThemeName(final int themeId) {
        final KeyboardTheme theme = searchKeyboardThemeById(themeId, KEYBOARD_THEMES);
        return theme.mThemeName;
    }

    public static void saveKeyboardThemeId(final int themeId, final SharedPreferences prefs) {
        saveKeyboardThemeId(themeId, prefs, BuildCompatUtils.EFFECTIVE_SDK_INT);
    }

    /* package private for testing */
    static String getPreferenceKey(final int sdkVersion) {
        if (sdkVersion <= VERSION_CODES.KITKAT) {
            return KLP_KEYBOARD_THEME_KEY;
        }
        return LXX_KEYBOARD_THEME_KEY;
    }

    /* package private for testing */
    static void saveKeyboardThemeId(final int themeId, final SharedPreferences prefs,
                                    final int sdkVersion) {
        final String prefKey = getPreferenceKey(sdkVersion);
        prefs.edit().putString(prefKey, Integer.toString(themeId)).apply();

        if (themeId == THEME_ID_CUSTOM) {
            saveThemeType(prefs, THEME_TYPE_CUSTOM);
        } else {
            saveThemeType(prefs, THEME_TYPE_BUILT_IN);
        }

    }

    public static KeyboardTheme getKeyboardTheme(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final KeyboardTheme[] availableThemeArray = getAvailableThemeArray(context);
        return getKeyboardTheme(prefs, BuildCompatUtils.EFFECTIVE_SDK_INT, availableThemeArray);
    }

    /* package private for testing */
    static KeyboardTheme[] getAvailableThemeArray(final Context context) {
        if (AVAILABLE_KEYBOARD_THEMES == null) {
            final int[] availableThemeIdStringArray = context.getResources().getIntArray(
                    R.array.keyboard_theme_ids);
            final ArrayList<KeyboardTheme> availableThemeList = new ArrayList<>();
            for (final int id : availableThemeIdStringArray) {
                final KeyboardTheme theme = searchKeyboardThemeById(id, KEYBOARD_THEMES);
                if (theme != null) {
                    availableThemeList.add(theme);
                }
            }
            AVAILABLE_KEYBOARD_THEMES = availableThemeList.toArray(
                    new KeyboardTheme[availableThemeList.size()]);
            Arrays.sort(AVAILABLE_KEYBOARD_THEMES);
        }
        return AVAILABLE_KEYBOARD_THEMES;
    }

    /* package private for testing */
    static KeyboardTheme getKeyboardTheme(final SharedPreferences prefs, final int sdkVersion,
                                          final KeyboardTheme[] availableThemeArray) {
        final String lxxThemeIdString = prefs.getString(LXX_KEYBOARD_THEME_KEY, null);
        if (lxxThemeIdString == null) {
            return getDefaultKeyboardTheme(prefs, sdkVersion, availableThemeArray);
        }
        try {
            final int themeId = Integer.parseInt(lxxThemeIdString);
            final KeyboardTheme theme = searchKeyboardThemeById(themeId, availableThemeArray);
            if (theme != null) {
                return theme;
            }
            Log.w(TAG, "Unknown keyboard theme in LXX preference: " + lxxThemeIdString);
        } catch (final NumberFormatException e) {
            Log.w(TAG, "Illegal keyboard theme in LXX preference: " + lxxThemeIdString, e);
        }
        // Remove preference that contains unknown or illegal theme id.
        prefs.edit().remove(LXX_KEYBOARD_THEME_KEY).apply();
        return getDefaultKeyboardTheme(prefs, sdkVersion, availableThemeArray);
    }

    public static int getThemeType(SharedPreferences pref) {
        return pref.getInt(THEME_TYPE_KEY, THEME_TYPE_BUILT_IN);
    }

    public static void saveThemeType(SharedPreferences pref, int themeType) {
        pref.edit().putInt(THEME_TYPE_KEY, themeType).apply();
    }

    public static void saveCustomSelectedThemeId(SharedPreferences pref, int id) {
        pref.edit().putInt(CUSTOM_THEME_ID_KEY, id).apply();
    }

    public static int getCustomSelectedThemeId(SharedPreferences pref) {
        return pref.getInt(CUSTOM_THEME_ID_KEY, 0);
    }

    @Override
    public int compareTo(final KeyboardTheme rhs) {
        if (mMinApiVersion > rhs.mMinApiVersion) return -1;
        if (mMinApiVersion < rhs.mMinApiVersion) return 1;
        return 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        return (o instanceof KeyboardTheme) && ((KeyboardTheme) o).mThemeId == mThemeId;
    }

    @Override
    public int hashCode() {
        return mThemeId;
    }
}
