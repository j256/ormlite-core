package com.j256.ormlite.field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.LazyForeignCollection;

/**
 * Annotation that identifies a {@link ForeignCollection} field in a class that corresponds to objects in a foreign
 * table that match the foreign-id of the current class.
 * 
 * <p>
 * <blockquote>
 * 
 * <pre>
 * &#064;ForeignCollection(id = true)
 * private ForeignCollection&lt;Order&gt; orders;
 * </pre>
 * 
 * </blockquote>
 * </p>
 * 
 * @author graywatson
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface ForeignCollectionField {

	/**
	 * @see #maxForeignCollectionLevel()
	 */
	public static final int MAX_FOREIGN_COLLECTION_LEVEL = 0;

	/**
	 * Set to true if the collection is a an eager collection where all of the results should be retrieved when the
	 * parent object is retrieved. Default is false (lazy) when the results will not be retrieved until you ask for the
	 * iterator from the collection.
	 * 
	 * <p>
	 * <b>NOTE:</b> If this is false (i.e. we have a lazy collection) then a connection is held open to the database as
	 * you iterate through the collection. This means that you need to make sure it is closed when you finish. See
	 * {@link LazyForeignCollection#iterator()} for more information.
	 * </p>
	 */
	boolean eager() default false;

	/**
	 * Set this to be the number of times to configure a foreign collection's foreign collection. If you query for A and
	 * it has an foreign-collection of field B which has an foreign-collection of field C ..., then a lot of
	 * configuration information is being stored. If each of these fields is eager-fetched, then querying for A could
	 * get expensive. By default this value is 0 meaning that it will only do the root collection and not a collection
	 * of a collection. It should be increased if you know what you are doing.
	 */
	int maxForeignCollectionLevel() default MAX_FOREIGN_COLLECTION_LEVEL;
}
