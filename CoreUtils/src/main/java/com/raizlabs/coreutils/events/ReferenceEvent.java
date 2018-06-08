package com.raizlabs.coreutils.events;

import com.raizlabs.coreutils.collections.TransactionalHashSet;
import com.raizlabs.coreutils.functions.Delegate;
import com.raizlabs.coreutils.functions.DelegateSet;

import java.lang.ref.Reference;

/**
 * Base reference event class that subclasses share logic for.
 */
abstract class ReferenceEvent<T, ReferenceClass extends Reference<Delegate<T>>> implements IEvent<T> {

    private TransactionalHashSet<ReferenceClass> listeners;

    public ReferenceEvent() {
        listeners = new TransactionalHashSet<>();
    }

    @Override
    public void addListener(Delegate<T> listener) {
        listeners.add(createReference(listener));
    }

    @Override
    public boolean removeListener(Delegate<T> listener) {
        return listeners.remove(createReference(listener));
    }

    protected abstract ReferenceClass createReference(Delegate<T> listener);

    @Override
    public void raiseEvent(T params) {
        listeners.beginTransaction();
        for (ReferenceClass reference : listeners) {
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

}
