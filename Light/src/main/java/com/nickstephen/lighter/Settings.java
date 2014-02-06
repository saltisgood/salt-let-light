package com.nickstephen.lighter;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * Created by Nick Stephen on 2/02/14.
 */
public class Settings extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < 11) {
            this.addPreferencesFromResource(R.xml.preferences);
        } else {
            this.getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFrag())
                    .commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class SettingsFrag extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.addPreferencesFromResource(R.xml.preferences);
        }
    }
}
