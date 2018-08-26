package com.j256.ormlite.field.types;

import java.nio.charset.Charset;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;

/**
 * Type that persists a binary byte[] object.
 *
 * @author Noor Dawod, noor@fineswap.com
 * @since August 26, 2018
 */
public class BinaryType extends ByteArrayType {

	private static final BinaryType singleTon = new BinaryType();
	private static final Charset ASCII = Charset.forName("ASCII");

	public static BinaryType getSingleton() {
		return singleTon;
	}

	private BinaryType() {
		this(SqlType.BYTE_ARRAY, new Class<?>[0]);
	}

	/**
	 * Here for others to subclass.
	 */
	protected BinaryType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
		if (defaultStr == null) {
			return null;
		} else {
			return defaultStr.getBytes(ASCII);
		}
	}

	@Override
	public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) {
		return stringValue.getBytes(ASCII);
	}
}
