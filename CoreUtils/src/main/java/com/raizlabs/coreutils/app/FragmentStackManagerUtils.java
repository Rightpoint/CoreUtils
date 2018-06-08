package com.raizlabs.coreutils.app;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.Locale;

public class FragmentStackManagerUtils {

    /**
     * Obtains a {@link FragmentStackManagerFragment} for the given container
     * via the {@link FragmentManager} in the given activity.
     * This will obtain any previous manager that still exists or create one
     * if one can't be found.
     * @param activity The {@link FragmentActivity} whose {@link FragmentManager}
     * should be used
     * @param containerID The resource ID of the container the manager will be
     * bound to.
     * @return The manager bound to the given container
     */
    public static FragmentStackManagerFragment getFragmentStackManager(FragmentActivity activity, int containerID) {
        return getFragmentStackManager(activity.getSupportFragmentManager(), containerID);
    }

    public static FragmentStackManagerFragment getFragmentStackManager(FragmentManager manager, int containerID) {
        final String tag = getFragmentStackManagerTag(containerID);

        // Get any FragmentStackManager if one exists
        FragmentStackManagerFragment stackManager = (FragmentStackManagerFragment)
                manager.findFragmentByTag(tag);

        // Create and store one if it does not
        if (stackManager == null) {
            stackManager = FragmentStackManagerFragment.newInstance(containerID);

            manager.beginTransaction()
                    .add(stackManager, tag)
                    .commit();
        }

        return stackManager;
    }

    private static String getFragmentStackManagerTag(int containerID) {
        return String.format(Locale.getDefault(),"StackManager%d", containerID);
    }
}
