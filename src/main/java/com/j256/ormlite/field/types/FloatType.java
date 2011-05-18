package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;

/**
 * Type that persists a float primitive.
 * 
 * @author graywatson
 */
public class FloatType extends FloatObjectType {

	private static final FloatType singleTon = new FloatType();

	public static FloatType getSingleton() {
		return singleTon;
	}

	private FloatType() {
		super(SqlType.FLOAT, new Class<?>[] { float.class });
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}
}
