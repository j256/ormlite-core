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

	private static final String DEFAULT_STRING_BYTES_CHARSET_NAME = "Unicode";

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
		if (defaultStr == null) {
			return null;
		} else {
			try {
				return defaultStr.getBytes(getCharsetName(fieldType));
			} catch (UnsupportedEncodingException e) {
				throw SqlExceptionUtil.create("Could not convert default string: " + defaultStr, e);
			}
		}
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
		try {
			return stringValue.getBytes(getCharsetName(fieldType));
		} catch (UnsupportedEncodingException e) {
			throw SqlExceptionUtil.create("Could not convert default string: " + stringValue, e);
		}
	}

	@Override
	public Class<?> getPrimaryClass() {
		return byte[].class;
	}

	private String getCharsetName(FieldType fieldType) {
		if (fieldType == null || fieldType.getFormat() == null) {
			return DEFAULT_STRING_BYTES_CHARSET_NAME;
		} else {
			return fieldType.getFormat();
		}
	}
}
