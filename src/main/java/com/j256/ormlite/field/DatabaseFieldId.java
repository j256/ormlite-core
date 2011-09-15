package com.j256.ormlite.field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.j256.ormlite.db.DatabaseType;

/**
 * Additions to the {@link DatabaseFieldSimple} annotations which _must_ be specified for this annotation to be
 * detected.
 * 
 * @author graywatson
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface DatabaseFieldId {

	/**
	 * Whether the field is the id field or not. Default is false. Only one field can have this set in a class. If you
	 * don't have it set then you won't be able to use the query, update, and delete by ID methods. Only one of this,
	 * {@link #generatedId}, and {@link #generatedIdSequence} can be specified.
	 */
	boolean id() default false;

	/**
	 * Whether the field is an auto-generated id field. Default is false. With databases for which
	 * {@link DatabaseType#isIdSequenceNeeded} is true then this will cause the name of the sequence to be
	 * auto-generated. To specify the name of the sequence use {@link #generatedIdSequence}. Only one of this,
	 * {@link #id}, and {@link #generatedIdSequence} can be specified.
	 */
	boolean generatedId() default false;

	/**
	 * The name of the sequence number to be used to generate this value. Default is none. This is only necessary for
	 * database for which {@link DatabaseType#isIdSequenceNeeded} is true and you already have a defined sequence that
	 * you want to use. If you use {@link #generatedId} instead then the code will auto-generate a sequence name. Only
	 * one of this, {@link #id}, and {@link #generatedId} can be specified.
	 */
	String generatedIdSequence() default "";

	/**
	 * If this is set to true then inserting an object with the ID field already set will not override it with a
	 * generated-id. This is useful when you have a table where items sometimes have IDs and sometimes need them
	 * generated. This only works if the database supports this behavior and if {@link #generatedId()} is also true for
	 * the field.
	 */
	boolean allowGeneratedIdInsert() default false;
}
