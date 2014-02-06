package com.nickstephen.lighter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

/**
 * Created by Nick Stephen on 2/02/14.
 */
public final class SettingsAccessor {
    private static final String PREF_KEY_BRIGHTNESS_LEVEL = "pref_key_brightness_level";
    private static final String PREF_KEY_SCREEN_ON = "pref_key_screen_on";

    private SettingsAccessor() {}

    public static boolean shouldManageSystemBrightness(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_update_system_key), true);
    }

    public static boolean shouldRememberBrightness(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_remember_brightness_key), true);
    }

    public static void saveBrightnessLevel(Context context, int brightness) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(PREF_KEY_BRIGHTNESS_LEVEL, brightness);
        if (Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static int getBrightnessLevel(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_KEY_BRIGHTNESS_LEVEL, -1);
    }

    public static boolean shouldKeepScreenOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_KEY_SCREEN_ON, true);
    }

    public static void setShouldKeepScreenOn(Context context, boolean screenOn) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(PREF_KEY_SCREEN_ON, screenOn);
        if (Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static int getFavScreen(Context context) {
        String key = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_fav_screen_key), context.getString(R.string.pref_fav_screen_default));

        if (key.compareTo("torch") == 0) {
            return 0;
        } else {
            return 1;
        }
    }
}
