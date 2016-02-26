package com.raizlabs.coreutils.events;

import com.raizlabs.coreutils.util.observable.lists.ListObserver;
import com.raizlabs.coreutils.util.observable.lists.ListObserverListener;

import java.lang.ref.WeakReference;

public abstract class WeakDelegateListObserverListener<Data, Delegate> implements ListObserverListener<Data> {

    private WeakReference<Delegate> weakRef;

    /**
     * Constructs a new listener which weakly references the given delegate.
     * @param delegate The delegate to weakly reference.
     */
    public WeakDelegateListObserverListener(Delegate delegate) {
        this.weakRef = new WeakReference<Delegate>(delegate);
    }

    /**
     * Called to attempt to retrieve the delegate. If it can't be reached, we
     * will attempt to remove ourselves from the given observer.
     * @param observer An observer to attempt to remove this listener from if
     *                 we can't reach the delegate.
     * @return The delegate, or null if it could not be reached.
     */
    protected Delegate getDelegate(ListObserver<Data> observer) {
        Delegate proxy = weakRef.get();
        if (proxy != null) {
            return proxy;
        } else {
            if (observer != null) {
                observer.removeListener(this);
            }
            return null;
        }
    }
}
