package com.j256.ormlite.field.types;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Arrays;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Type that persists a byte[] object.
 *
 * @author graywatson
 */
public class ByteArrayType extends BaseDataType {

	private static final ByteArrayType singleTon = new ByteArrayType();

	public static ByteArrayType getSingleton() {
		return singleTon;
	}

	private ByteArrayType() {
		super(SqlType.BYTE_ARRAY);
	}

	/**
	 * Here for others to subclass.
	 */
	protected ByteArrayType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		return defaultStr == null ? null : getBytesImpl(fieldType, defaultStr);
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return (byte[]) results.getBytes(columnPos);
	}

	@Override
	public boolean isArgumentHolderRequired() {
		return true;
	}

	@Override
	public boolean dataIsEqual(Object fieldObj1, Object fieldObj2) {
		if (fieldObj1 == null) {
			return (fieldObj2 == null);
		} else if (fieldObj2 == null) {
			return false;
		} else {
			return Arrays.equals((byte[]) fieldObj1, (byte[]) fieldObj2);
		}
	}

	@Override
	public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) throws SQLException {
		return getBytesImpl(fieldType, stringValue);
	}

	@Override
	public Class<?> getPrimaryClass() {
		return byte[].class;
	}

	private Object getBytesImpl(FieldType fieldType, String stringValue) throws SQLException {
		if (fieldType == null || fieldType.getFormat() == null) {
			return stringValue.getBytes();
		} else {
			try {
				return stringValue.getBytes(fieldType.getFormat());
			} catch (UnsupportedEncodingException e) {
				throw SqlExceptionUtil.create("Could not convert default string: " + stringValue, e);
			}
		}
	}
}
