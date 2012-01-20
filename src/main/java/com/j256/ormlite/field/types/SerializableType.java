package com.j256.ormlite.field.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Persists an unknown Java Object that is {@link Serializable}.
 * 
 * @author graywatson
 */
public class SerializableType extends BaseDataType {

	private static final SerializableType singleTon = new SerializableType();

	public static SerializableType getSingleton() {
		return singleTon;
	}

	private SerializableType() {
		/*
		 * NOTE: Serializable class should _not_ be in the list because _everything_ is serializable and we want to
		 * force folks to use DataType.SERIALIZABLE -- especially for forwards compatibility.
		 */
		super(SqlType.SERIALIZABLE, new Class<?>[0]);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		throw new SQLException("Default values for serializable types are not supported");
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return results.getBytes(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
		byte[] bytes = (byte[]) sqlArg;
		try {
			ObjectInputStream objInStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
			return objInStream.readObject();
		} catch (Exception e) {
			throw SqlExceptionUtil.create("Could not read serialized object from byte array: " + Arrays.toString(bytes)
					+ "(len " + bytes.length + ")", e);
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) throws SQLException {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
			objOutStream.writeObject(obj);
			return outStream.toByteArray();
		} catch (Exception e) {
			throw SqlExceptionUtil.create("Could not write serialized object to byte array: " + obj, e);
		}
	}

	@Override
	public boolean isValidForField(Field field) {
		return Serializable.class.isAssignableFrom(field.getType());
	}

	@Override
	public boolean isStreamType() {
		// can't do a getObject call beforehand so we have to check for nulls
		return true;
	}

	@Override
	public boolean isComparable() {
		return false;
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}

	@Override
	public boolean isArgumentHolderRequired() {
		return true;
	}
}
