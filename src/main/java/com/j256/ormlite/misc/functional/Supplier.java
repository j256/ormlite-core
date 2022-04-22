package com.j256.ormlite.misc.functional;

/**
 * Represents a supplier of results.
 *
 * There is no requirement that a new or distinct result be returned each time the supplier is invoked.
 *
 * This is a functional interface intended to support lambda expressions in ormlite whose functional method is get().
 */
public interface Supplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get();
}
