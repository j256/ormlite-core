package com.j256.ormlite.field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This can be used <i>instead of</i> {@link DatabaseField}. It has fewer fields which can give a performance boost on
 * some architectures -- namely Android. Other fields can be specified with the other @DatabaseField... annotations.
 * 
 * <p>
 * <b>NOTE:</b> If you use @DatabaseField then you should not use this @DatabaseFieldSimple annotation or any of the
 * other @DatabaseField... annotations. They will be ignored.
 * </p>
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
