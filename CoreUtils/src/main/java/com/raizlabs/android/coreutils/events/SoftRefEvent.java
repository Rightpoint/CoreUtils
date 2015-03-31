package com.raizlabs.android.coreutils.events;

import com.raizlabs.android.coreutils.collections.TransactionalHashSet;
import com.raizlabs.android.coreutils.functions.Delegate;
import com.raizlabs.android.coreutils.functions.DelegateSet;

import java.lang.ref.SoftReference;

/**
 * Implementation of {@link IEvent} which uses soft references to its listeners.
 *
 * @param <T> The parameter type of the event.
 */
public class SoftRefEvent<T> implements IEvent<T> {

    private TransactionalHashSet<DelegateReference<T>> listeners;

    public SoftRefEvent() {
        listeners = new TransactionalHashSet<>();
    }

    @Override
    public void addListener(Delegate<T> listener) {
        listeners.add(new DelegateReference<>(listener));
    }

    @Override
    public boolean removeListener(Delegate<T> listener) {
        return listeners.remove(new DelegateReference<>(listener));
    }

    @Override
    public void raiseEvent(T params) {
        listeners.beginTransaction();
        for (DelegateReference<T> reference : listeners) {
            Delegate<T> listener = reference.get();

            if (listener != null) {
                listener.execute(params);
            } else {
                listeners.remove(reference);
            }
        }
        listeners.endTransaction();
    }

    /**
     * Called to actually raise the event. Subclasses may override this in order
     * to perform custom logic, but should be sure to execute the event across
     * the given listeners.
     *
     * @param listeners A set of listeners which need to be notified.
     * @param params    The parameters to be sent to each listener.
     */
    protected void performRaiseEvent(DelegateSet<T> listeners, T params) {
        listeners.execute(params);
    }

    private static class DelegateReference<T> extends SoftReference<Delegate<T>> {

        public DelegateReference(Delegate<T> r) {
            super(r);
        }

        @Override
        public boolean equals(Object o) {
            boolean superEquals = super.equals(o);

            if (!superEquals) {
                Delegate<T> ref = get();

                if (ref != null) {
                    return ref.equals(o);
                } else if (o == null) {
                    return true;
                }
            }

            return superEquals;
        }
    }
}
