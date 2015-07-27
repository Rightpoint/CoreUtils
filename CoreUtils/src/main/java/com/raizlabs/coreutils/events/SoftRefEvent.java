package com.raizlabs.coreutils.events;

import com.raizlabs.coreutils.functions.Delegate;

import java.lang.ref.SoftReference;

/**
 * Implementation of {@link ReferenceEvent} which uses soft references to its listeners.
 *
 * @param <T> The parameter type of the event.
 */
public class SoftRefEvent<T> extends ReferenceEvent<T, SoftRefEvent.SoftDelegateReference<T>> {

    public SoftRefEvent() {
    }

    @Override
    protected SoftDelegateReference<T> createReference(Delegate<T> listener) {
        return new SoftDelegateReference<>(listener);
    }

    static class SoftDelegateReference<T> extends SoftReference<Delegate<T>> {

        public SoftDelegateReference(Delegate<T> r) {
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
