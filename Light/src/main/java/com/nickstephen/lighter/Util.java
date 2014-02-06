package com.nickstephen.lighter;

import android.util.Log;

/**
 * Created by Nick Stephen on 6/02/14.
 */
public final class Util {
    public static final String DEV_URI = "market://search?q=pub:Nicholas+Stephen";

    public static final boolean IS_DEBUG = false;

    private Util() {}

    public static void logD(String tag, String msg) {
        if (IS_DEBUG) {
            Log.d(tag, msg);
        }
    }
}
