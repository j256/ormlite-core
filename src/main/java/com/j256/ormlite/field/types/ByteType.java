package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;

/**
 * Type that persists a byte primitive.
 * 
 * @author graywatson
 */
public class ByteType extends ByteObjectType {

	private static final ByteType singleTon = new ByteType();

	public static ByteType getSingleton() {
		return singleTon;
	}

	private ByteType() {
		super(SqlType.BYTE, new Class<?>[] { byte.class });
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}
}
