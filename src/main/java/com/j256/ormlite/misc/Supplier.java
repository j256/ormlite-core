package com.j256.ormlite.misc;

/**
 * Represents a supplier of results. There is no requirement that a new or distinct result be returned each time the
 * supplier is invoked.
 *
 * This is a functional interface intended to support lambda expressions in ormlite whose functional method is get() for
 * those folks not using JDK 8.
 */
public interface Supplier<T> {

	/**
	 * Return the supplied result.
	 */
	public T get();
}
