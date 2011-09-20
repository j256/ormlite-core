package com.j256.ormlite.field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Similar to {@link DatabaseField} but with fewer fields. Other fields can be specified with the other @DatabaseField*
 * annotations.
 * 
 * @author graywatson
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface DatabaseFieldSimple {

	/**
	 * @see DatabaseField#columnName()
	 */
	String columnName() default "";

	/**
	 * @see DatabaseField#defaultValue()
	 */
	String defaultValue() default DatabaseField.DEFAULT_STRING;

	/**
	 * @see DatabaseField#width()
	 */
	int width() default 0;

	/**
	 * @see DatabaseField#canBeNull()
	 */
	boolean canBeNull() default true;
}
