package com.raizlabs.coreutils.util;

/**
 * A {@link Converter} converts objects from one type to another.
 * @param <From> The type to convert from.
 * @param <To> The type to convert to.
 */
public interface Converter<From, To> {
    /**
     * Called to convert the given object to the destination type.
     * @param from The object to convert from.
     * @return The converted object.
     */
    To convert(From from);
}
