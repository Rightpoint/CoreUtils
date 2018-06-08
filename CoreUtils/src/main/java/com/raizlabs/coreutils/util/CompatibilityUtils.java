package com.raizlabs.coreutils.util;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

public class CompatibilityUtils {

    /**
     * @return True if the current device runs {@link VERSION_CODES#LOLLIPOP} or higher
     */
    public static boolean isEqualToOrAboveLollipop() {
        return VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    }

    /**
     * @return True if the current device runs {@link VERSION_CODES#KITKAT} or higher
     */
    public static boolean isEqualToOrAboveKitKat() {
        return VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }

    /**
     * @return True if the current device runs {@link VERSION_CODES#ICE_CREAM_SANDWICH} or higher
     */
    public static boolean isEqualToOrAboveICS() {
        return VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;
    }
}