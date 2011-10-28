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
	int maxForeignAutoRefreshLevel() default DatabaseField.DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL;

	/**
	 * @see DatabaseField#foreignAutoCreate()
	 */
	boolean foreignAutoCreate() default false;
}
