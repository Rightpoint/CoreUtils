package com.raizlabs.coreutils.events;

import com.raizlabs.coreutils.functions.Delegate;

import java.lang.ref.WeakReference;

/**
 * Implementation of {@link ReferenceEvent} which uses weak references to its listeners.
 *
 * @param <T> The parameter type of the event.
 */
public class WeakRefEvent<T> extends ReferenceEvent<T, WeakRefEvent.WeakDelegateReference<T>> {

    @Override
    protected WeakDelegateReference<T> createReference(Delegate<T> listener) {
        return new WeakDelegateReference<>(listener);
    }

    static class WeakDelegateReference<T> extends WeakReference<Delegate<T>> {

        public WeakDelegateReference(Delegate<T> r) {
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
