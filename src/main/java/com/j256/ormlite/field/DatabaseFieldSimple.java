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

	/** this special string is used as a .equals check to see if no default was specified */
	public static final String NO_DEFAULT = "__ormlite__ no default value string was specified";

	/**
	 * The name of the column in the database. If not set then the name is taken from the field name.
	 */
	String columnName() default "";

	/**
	 * The default value of the field for creating the table. Default is none.
	 */
	String defaultValue() default NO_DEFAULT;

	/**
	 * Width of array fields (often for strings). Default is 0 which means to take the data-type and database-specific
	 * default. For strings that means 255 characters although some databases do not support this.
	 */
	int width() default 0;

	/**
	 * Whether the field can be assigned to null or have no value. Default is true.
	 */
	boolean canBeNull() default true;
}
