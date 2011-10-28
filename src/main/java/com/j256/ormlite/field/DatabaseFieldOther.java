package com.j256.ormlite.field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.j256.ormlite.field.types.VoidType;

/**
 * @deprecated Please use just the {@link DatabaseField} annotation. This annotation was created when we found
 *             performance problems in the Android annotations. We have a work around for these annotations now that
 *             makes them adequately fast. These are causing confusion so we've decided to pull them. Sorry.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Deprecated
public @interface DatabaseFieldOther {

	/**
	 * @see DatabaseField#dataType()
	 */
	DataType dataType() default DataType.UNKNOWN;

	/**
	 * @see DatabaseField#unknownEnumName()
	 */
	String unknownEnumName() default "";

	/**
	 * @see DatabaseField#throwIfNull()
	 */
	boolean throwIfNull() default false;

	/**
	 * @see DatabaseField#format()
	 */
	String format() default "";

	/**
	 * @see DatabaseField#persisterClass()
	 */
	Class<? extends DataPersister> persisterClass() default VoidType.class;

	/**
	 * @see DatabaseField#useGetSet()
	 */
	boolean useGetSet() default false;

	/**
	 * @see DatabaseField#columnDefinition()
	 */
	String columnDefinition() default "";

	/**
	 * @see DatabaseField#version()
	 */
	boolean version() default false;
}
