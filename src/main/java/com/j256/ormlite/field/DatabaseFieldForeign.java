package com.j256.ormlite.field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Additions to the {@link DatabaseFieldSimple} annotations which _must_ be specified for this annotation to be
 * detected.
 * 
 * @author graywatson
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface DatabaseFieldForeign {

	/**
	 * @see DatabaseField#foreign()
	 */
	boolean foreign() default false;

	/**
	 * @see DatabaseField#foreignAutoRefresh()
	 */
	boolean foreignAutoRefresh() default false;

	/**
	 * @see DatabaseField#maxForeignAutoRefreshLevel()
	 */
	int maxForeignAutoRefreshLevel() default DatabaseField.MAX_FOREIGN_AUTO_REFRESH_LEVEL;
}
