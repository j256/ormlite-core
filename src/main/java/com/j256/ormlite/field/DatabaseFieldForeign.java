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
	 * Default for the maxForeignAutoRefreshLevel.
	 * 
	 * @see #maxForeignAutoRefreshLevel()
	 */
	public static final int MAX_FOREIGN_AUTO_REFRESH_LEVEL = 2;

	/**
	 * Field is a non-primitive object that corresponds to another class that is also stored in the database. It must
	 * have an id field (either {@link #id}, {@link #generatedId}, or {@link #generatedIdSequence} which will be stored
	 * in this table. When an object is returned from a query call, any foreign objects will just have the id field set
	 * in it. To get all of the other fields you will have to do a refresh on the object using its own Dao.
	 */
	boolean foreign() default false;

	/**
	 * Set this to be true (default false) to have a foreign field automagically refreshed when an object is queried.
	 * This will _not_ automagically create the foreign object but when the object is queried, a separate database call
	 * will be made to load of the fields of the foreign object via an internal DAO. The default is to just have the ID
	 * field in the object retrieved and for the caller to call refresh on the correct DAO.
	 * 
	 * <p>
	 * <b>NOTE:</b> This will create another DAO object so that low memory devices may want to call refresh by hand.
	 * </p>
	 */
	boolean foreignAutoRefresh() default false;

	/**
	 * Set this to be the number of times to configure a foreign object's foreign object. If you query for A and it has
	 * an foreign field B which has an foreign field C ..., then a lot of configuration information is being stored. If
	 * each of these fields is auto-refreshed, then querying for A could get expensive. Setting this value to 1 will
	 * mean that when you query for A, B will be auto-refreshed, but C will just have its id field set. This also works
	 * if A has an auto-refresh field B which has an auto-refresh field A.
	 * 
	 * <p>
	 * <b>NOTE:</b> Increasing this value will result in more database transactions whenever you query for A, so use
	 * carefully.
	 * </p>
	 */
	int maxForeignAutoRefreshLevel() default MAX_FOREIGN_AUTO_REFRESH_LEVEL;
}
