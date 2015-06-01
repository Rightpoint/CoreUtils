package com.raizlabs.android.coreutils.functions;

/**
 * Interface for a predicate which evaluates if a given item fulfills a certain condition
 *
 * @param <T> The type of item to evaluate.
 */
public interface Predicate<T> {
    /**
     * Evaluates the given item.
     *
     * @param item The item to evaluate.
     * @return The true or false value for the given item.
     */
    public boolean evaluate(T item);
}
