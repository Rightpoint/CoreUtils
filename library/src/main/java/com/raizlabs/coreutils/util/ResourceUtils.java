package com.raizlabs.coreutils.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Build;

public class ResourceUtils {

    /**
     * Retrieves the color for the given ID from the specified {@link Context} and, if applicable, its associated
     * {@link Theme}.
     * @see #getColor(Resources, int, Theme)
     * @param context The context to retrieve the color from.
     * @param id The ID of the color to retrieve.
     * @return The retrieved color.
     */
    public static int getColor(Context context, int id) {
        return getColor(context.getResources(), id, context.getTheme());
    }

    /**
     * Retrieves the color for the given ID from the specified {@link Resources} and {@link Theme}. This will look up
     * using the theme if the system API is available, otherwise it will just use the default.
     * @param resources The resources to retrieve the color from.
     * @param id The ID of the color to retrieve.
     * @param theme The theme to use to retrieve the color.
     * @return The retrieved color.
     */
    public static int getColor(Resources resources, int id, Theme theme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return resources.getColor(id, theme);
        } else {
            return resources.getColor(id);
        }
    }
}
