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
public @interface DatabaseFieldId {

	/**
	 * @see DatabaseField#id()
	 */
	boolean id() default false;

	/**
	 * @see DatabaseField#generatedId()
	 */
	boolean generatedId() default false;

	/**
	 * @see DatabaseField#generatedIdSequence()
	 */
	String generatedIdSequence() default "";

	/**
	 * @see DatabaseField#allowGeneratedIdInsert()
	 */
	boolean allowGeneratedIdInsert() default false;
}
