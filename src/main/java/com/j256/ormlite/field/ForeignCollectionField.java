package com.j256.ormlite.field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.j256.ormlite.dao.ForeignCollection;

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
	 * Set to true if the collection is a an eager collection where all of the results should be retrieved when the
	 * parent object is retrieved. Default is false (lazy) when the results will not be retrieved until you ask for the
	 * iterator from the collection.
	 */
	boolean eager() default false;
}
