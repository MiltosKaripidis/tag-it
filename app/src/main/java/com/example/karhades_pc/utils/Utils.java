package com.example.karhades_pc.utils;

import android.os.Build;

/**
 * Created by Karhades on 05-11-15.
 */
public class Utils {

    private Utils() {
        // CANNOT CONSTRUCT.
    }

    public static boolean itSupportsTransitions() {
        return Build.VERSION.SDK_INT >= 21;
    }
}
