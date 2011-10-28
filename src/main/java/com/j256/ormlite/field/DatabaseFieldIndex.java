package com.j256.ormlite.field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @deprecated Please use just the {@link DatabaseField} annotation. This annotation was created when we found
 *             performance problems in the Android annotations. We have a work around for these annotations now that
 *             makes them adequately fast. These are causing confusion so we've decided to pull them. Sorry.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Deprecated
public @interface DatabaseFieldIndex {

	/**
	 * @see DatabaseField#unique()
	 */
	boolean unique() default false;

	/**
	 * @see DatabaseField#uniqueCombo()
	 */
	boolean uniqueCombo() default false;

	/**
	 * @see DatabaseField#index()
	 */
	boolean index() default false;

	/**
	 * @see DatabaseField#uniqueIndex()
	 */
	boolean uniqueIndex() default false;

	/**
	 * @see DatabaseField#indexName()
	 */
	String indexName() default "";

	/**
	 * @see DatabaseField#uniqueIndexName()
	 */
	String uniqueIndexName() default "";
}
