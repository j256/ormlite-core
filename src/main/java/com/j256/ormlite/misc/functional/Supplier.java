package com.j256.ormlite.misc.functional;

/**
 * This is a functional interface intended to support lambda expressions in ormlite.
 */
public interface Supplier<T> {

    /**
     * Gets a value.
     *
     * @return a value
     */
    T get();
}
