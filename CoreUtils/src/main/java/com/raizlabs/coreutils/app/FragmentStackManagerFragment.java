package com.raizlabs.coreutils.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.Locale;

/**
 * Class for a {@link Fragment} that is retained and manages a stack of {@link Fragment}s
 */
public class FragmentStackManagerFragment extends Fragment {

    /*
	 * This implementation works by tagging each fragment with a string that
	 * matches its index in the stack. This way, we can keep adding, removing,
	 * replacing, etc. without keeping pointers to each fragment. Since the
	 * strings can be reproduced solely by their index, we will be able to
	 * find any fragments later just by using the findFragmentByTag method
	 * in the FragmentManager. Since the system takes care of recreating
	 * fragments and restoring the fragments after orientation changes, we
	 * will always obtain the fragment which matches our current orientation.
	 *
	 * All we need to keep track of is how large our stack is (the index of
	 * the top fragment)
	 */

    //region Constants

    private static final String KEY_CONTAINER_ID = "containerID";
    private static final String KEY_STACK_TOP = "stackTop";

    //endregion Constants

    //region Statics

    /**
     * Creates a new {@link FragmentStackManagerFragment} which maintains a
     * stack inside the given container id.
     * @param containerID The ID of the container to commit fragments to.
     * @return The new {@link FragmentStackManagerFragment} instance
     */
    public static FragmentStackManagerFragment newInstance(int containerID) {
        FragmentStackManagerFragment manager = new FragmentStackManagerFragment();
        manager.setArguments(new Bundle());
        manager.setContainerID(containerID);
        return manager;
    }

    //endregion Statics

    //region Members

    private int currentStackTop;

    private int containerID;

    //endregion Members

    //region Constructors

    public FragmentStackManagerFragment() {
        super();
        currentStackTop = -1;
    }

    //endregion Constructors

    //region Accessors

    protected void setContainerID(int id) {
        getArguments().putInt(KEY_CONTAINER_ID, id);
        this.containerID = id;
    }

    //endregion Accessors

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        containerID = getArguments().getInt(KEY_CONTAINER_ID);
        // Retain the instance across orientation changes
        setRetainInstance(true);
    }

    //endregion Lifecycle

    //region Instance Methods

    /**
     * Pops fragments until the stack is empty.
     */
    public void clear() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        while (!isEmpty()) {
            popWithoutAttach(transaction);
        }
        transaction.commit();
    }

    /**
     * Gets the fragment currently associated with the given tag.
     * @param tag The tag, as returned by {@link #push(Fragment)}
     * @return The fragment currently associated with the given tag.
     */
    public Fragment getFragmentByTag(String tag) {
        return getFragmentManager().findFragmentByTag(tag);
    }

    /**
     * Pushes the given fragment on the top of the stack. This becomes the
     * newly visible fragment, while calling {@link #pop()} will return to
     * whatever fragment was on top before this call.
     * @param fragment The {@link Fragment} to push on the top of the stack
     * @return The identifying tag of the new fragment which can be used
     * to pop back down to this fragment, assuming it hasn't been removed
     * or replaced.
     */
    public String push(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        String newTag = push(fragment, transaction);
        transaction.commit();
        return newTag;
    }

    /**
     * Pops the current fragment off the top of the stack, only if there are
     * fragments underneath it to be shown. The new fragment on the top of the
     * stack will be shown.
     * @return True if there were more fragments underneath and the current
     * fragment was popped. False if we are already at the root or the stack
     * is empty.
     */
    public boolean pop() {
        // If we have more than just the 0 index, we have something underneath
        // so do the pop
        if (currentStackTop > 0) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            pop(transaction);
            transaction.commit();
            return true;
        } else {
            // We're at root or have no fragments
            return false;
        }
    }

    /**
     * Continually pops until the given tag is reached or we end up at the root
     * fragment. Note that this WILL remove all fragments except the root if
     * the tag has already been removed and we never see it.
     * @param tag The tag to pop to.
     * @return True if we are now sitting at a fragment with the given tag,
     * false if we ended up at the root fragment or the stack was empty to
     * begin with.
     */
    public boolean popToTag(String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        boolean success = popToTag(tag, transaction);
        transaction.commit();

        return success;
    }

    /**
     * Continually pops until the given tag is reached or we end up at the root
     * fragment and then pushes the given fragment on top.
     * @param tag The tag to pop to.
     * @param fragment The fragment to push over the one at the given tag.
     * @return The identifying tag of the new fragment which can be used
     * to pop back down to this fragment, assuming it hasn't been removed
     * or replaced.
     */
    public String popToTagAndPush(String tag, Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        popToTagWithoutAttach(tag, transaction);
        String newTag = push(fragment, transaction);
        transaction.commit();
        return newTag;
    }

    /**
     * Replaces the fragment with the given tag with the given fragment. This
     * will replace fragments that are not the top, so this change may not be
     * visible. This likely should be used in conjunction with
     * {@link #popToTag(String)}
     * @param tag The tag of the fragment to replace which will now map to the
     * new fragment if this returns true.
     * @param fragment The {@link Fragment} to replace the existing contents
     * with.
     * @return True if there was a fragment found for the given tag and was
     * replaced, false if no fragment was found.
     */
    public boolean replace(String tag, Fragment fragment) {
        Fragment victim = getFragmentManager().findFragmentByTag(tag);
        if (victim != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(containerID, fragment, tag);
            transaction.commit();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Replaces the fragment with the given tag with the given fragment. This
     * will replace fragments that are not the top, so this change may not be
     * visible. This likely should be used in conjunction with
     * {@link #popToTag(String)}
     * @param tag The tag of the fragment to replace which will now map to the
     * new fragment if this returns true.
     * @param fragment The {@link Fragment} to replace the existing contents
     * with.
     * @param inAnimationId <code>true</code> to allow the fragment transition to animate
     * with a crossfade. <code>false</code> for an instance change.
     * @return True if there was a fragment found for the given tag and was
     * replaced, false if no fragment was found.
     */
    public boolean replace(String tag, Fragment fragment, int inAnimationId, int outAnimationId) {
        if (getFragmentManager() == null) {
            // If you accidentally wind up calling replace() on a fragment that's been or is being
            // detached from the activity, don't try to do anything.
            return false;
        }

        Fragment victim = getFragmentManager().findFragmentByTag(tag);
        if (victim != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(inAnimationId, outAnimationId);
            transaction.replace(containerID, fragment, tag);
            transaction.commit();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Forces all transactions to be committed immediately.
     * @see FragmentManager#executePendingTransactions()
     */
    public void executePendingTransactions() {
        getFragmentManager().executePendingTransactions();
    }

    public void restoreInstanceState(Bundle bundle) {
        currentStackTop = bundle.getInt(KEY_STACK_TOP);
    }

    public void saveInstanceState(Bundle bundle) {
        bundle.putInt(KEY_STACK_TOP, currentStackTop);
    }

    /**
     * Returns the tag which identifies the current index in the stack. This
     * can be used to "remember" a position, push some fragments on top of it,
     * then restore back down to it using {@link #popToTag(String)}
     * @return The tag which identifies the current index in the stack.
     */
    public String getTopTag() {
        return getTagForIndex(currentStackTop);
    }

    /**
     * @return The {@link Fragment} which is currently on the top of the stack
     * and being displayed, or null if the stack is empty.
     */
    public Fragment getTop() {
        if (!isEmpty()) {
            return getFragmentManager().findFragmentByTag(getTopTag());
        } else {
            return null;
        }
    }

    /**
     * @return True if we're at the root of the stack.
     */
    public boolean isAtRootFragment() {
        return currentStackTop == 0;
    }

    /**
     * @return True if there are no {@link Fragment}s in the stack at all.
     */
    public boolean isEmpty() {
        return currentStackTop < 0;
    }

    protected String getTagForIndex(int index) {
        return String.format(Locale.getDefault(),"FragmentStack%d", index);
    }

    private String push(Fragment fragment, FragmentTransaction transaction) {
        // Get the current top and detach it. We will likely pop back to it
        // later so we don't want to do a full removal of the fragment
        // This also means we can still obtain it by tag if need be
        Fragment top = getTop();
        if (top != null) {
            transaction.detach(top);
        }

        // Add the given fragment to the next index
        String newTag = getTagForIndex(++currentStackTop);
        transaction.add(containerID, fragment, newTag);
        return newTag;
    }

    private void pop(FragmentTransaction transaction) {
        popWithoutAttach(transaction);
        Fragment newTop = getTop();
        transaction.attach(newTop);
    }

    private void popWithoutAttach(FragmentTransaction transaction) {
        // Fully remove the top fragment. We're doing a pop so we can't
        // ever return to it
        transaction.remove(getTop());

        // Decrement the top index and reattach the new top fragment
        // It was detached before by push()
        --currentStackTop;
    }

    private boolean popToTag(String tag, FragmentTransaction transaction) {
        boolean result = popToTagWithoutAttach(tag, transaction);
        Fragment top = getTop();
        if (top != null && top.isDetached()) {
            transaction.attach(top);
        }

        return result;
    }

    private boolean popToTagWithoutAttach(String tag, FragmentTransaction transaction) {
        while (!isEmpty() && !isAtRootFragment()) {
            if (!getTopTag().equals(tag)) {
                popWithoutAttach(transaction);
            } else {
                return true;
            }
        }

        return false;
    }

    //endregion Instance Methods
}
