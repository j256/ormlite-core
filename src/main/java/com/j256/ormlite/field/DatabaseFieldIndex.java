package com.j256.ormlite.field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This can be used instead of {@link DatabaseField}. This adds fields to the {@link DatabaseFieldSimple} annotations
 * which _must_ also be specified for this annotation to be detected.
 * 
 * @author graywatson
 */
@Target(FIELD)
@Retention(RUNTIME)
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
