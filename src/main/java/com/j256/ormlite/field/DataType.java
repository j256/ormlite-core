package com.j256.ormlite.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Data type enumeration to provide Java class to/from database mapping.
 * 
 * <p>
 * <b>NOTE:</b> If you add types here you will need to add to the various DatabaseType implementors' appendColumnArg()
 * method.
 * </p>
 * 
 * <p>
 * Here's a good page about the <a href="http://docs.codehaus.org/display/CASTOR/Type+Mapping" >mapping for a number of
 * database types</a>:
 * </p>
 * 
 * @author graywatson
 */
public enum DataType implements FieldConverter {

	/**
	 * Persists the {@link String} Java class.
	 */
	STRING(SqlType.STRING, null, new Class<?>[] { String.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return results.getString(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return defaultStr;
		}
	},

	/**
	 * Persists the boolean Java primitive.
	 */
	BOOLEAN(SqlType.BOOLEAN, null, new Class<?>[] { boolean.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Boolean) results.getBoolean(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Boolean.parseBoolean(defaultStr);
		}
		@Override
		public boolean isEscapeDefaultValue() {
			return false;
		}
		@Override
		public boolean isPrimitive() {
			return true;
		}
	},

	/**
	 * Persists the {@link Boolean} Java class.
	 */
	BOOLEAN_OBJ(SqlType.BOOLEAN, null, new Class<?>[] { Boolean.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Boolean) results.getBoolean(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Boolean.parseBoolean(defaultStr);
		}
		@Override
		public boolean isEscapeDefaultValue() {
			return false;
		}
	},

	/**
	 * Persists the {@link java.util.Date} Java class.
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 */
	JAVA_DATE(SqlType.DATE, null, new Class<?>[] { Date.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return new Date(results.getTimestamp(columnPos).getTime());
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			try {
				return new Timestamp(parseDateString(fieldType.getFormat(), defaultStr).getTime());
			} catch (ParseException e) {
				throw SqlExceptionUtil.create("Problems parsing default date string '" + defaultStr + "' using '"
						+ formatOrDefault(fieldType.getFormat()) + '\'', e);
			}
		}
		@Override
		public Object javaToArg(FieldType fieldType, Object javaObject) {
			Date date = (Date) javaObject;
			return new Timestamp(date.getTime());
		}
	},

	/**
	 * Persists the {@link java.util.Date} Java class as long milliseconds since epoch.
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 */
	JAVA_DATE_LONG(SqlType.LONG, null, new Class<?>[0]) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return new Date(results.getLong(columnPos));
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			try {
				return Long.parseLong(defaultStr);
			} catch (NumberFormatException e) {
				throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default date-long value: "
						+ defaultStr, e);
			}
		}
		@Override
		public Object javaToArg(FieldType fieldType, Object obj) {
			Date date = (Date) obj;
			return (Long) date.getTime();
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Persists the {@link java.util.Date} Java class as a string of a format.
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 * 
	 * <p>
	 * <b>WARNING:</b> Because of SimpleDateFormat not being reentrant, this has to do some synchronization with every
	 * data in/out unfortunately.
	 * </p>
	 */
	JAVA_DATE_STRING(SqlType.STRING, null, new Class<?>[0]) {

		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			String formatStr = fieldType.getFormat();
			String dateStr = results.getString(columnPos);
			try {
				return parseDateString(formatStr, dateStr);
			} catch (ParseException e) {
				throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing date-string '" + dateStr
						+ "' using '" + formatOrDefault(formatStr) + "'", e);
			}
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			try {
				// we parse to make sure it works and then format it again
				return normalizeDateString(fieldType.getFormat(), defaultStr);
			} catch (ParseException e) {
				throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default date-string '"
						+ defaultStr + "' using '" + formatOrDefault(fieldType.getFormat()) + "'", e);
			}
		}
		@Override
		public Object javaToArg(FieldType fieldType, Object obj) {
			Date date = (Date) obj;
			return formatDate(fieldType.getFormat(), date);
		}
	},

	/**
	 * Persists the byte primitive.
	 */
	BYTE(SqlType.BYTE, null, new Class<?>[] { byte.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Byte) results.getByte(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Byte.parseByte(defaultStr);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
		@Override
		public boolean isPrimitive() {
			return true;
		}
	},

	/**
	 * Persists the {@link Byte} Java class.
	 */
	BYTE_OBJ(SqlType.BYTE, null, new Class<?>[] { Byte.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Byte) results.getByte(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Byte.parseByte(defaultStr);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Persists the short primitive.
	 */
	SHORT(SqlType.SHORT, null, new Class<?>[] { short.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Short) results.getShort(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Short.parseShort(defaultStr);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
		@Override
		public boolean isPrimitive() {
			return true;
		}
	},

	/**
	 * Persists the {@link Short} Java class.
	 */
	SHORT_OBJ(SqlType.SHORT, null, new Class<?>[] { Short.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Short) results.getShort(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Short.parseShort(defaultStr);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Persists the int primitive.
	 */
	INTEGER(SqlType.INTEGER, SqlType.INTEGER, new Class<?>[] { int.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Integer) results.getInt(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Integer.parseInt(defaultStr);
		}
		@Override
		public Object convertIdNumber(Number number) {
			return (Integer) number.intValue();
		}
		@Override
		public Number resultToId(DatabaseResults results, int columnPos) throws SQLException {
			return (Integer) results.getInt(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
		@Override
		public boolean isPrimitive() {
			return true;
		}
	},

	/**
	 * Persists the {@link Integer} Java class.
	 */
	INTEGER_OBJ(SqlType.INTEGER, null, new Class<?>[] { Integer.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Integer) results.getInt(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Integer.parseInt(defaultStr);
		}
		@Override
		public Object convertIdNumber(Number number) {
			return (Integer) number.intValue();
		}
		@Override
		public Number resultToId(DatabaseResults results, int columnPos) throws SQLException {
			return (Integer) results.getInt(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Persists the long primitive.
	 */
	LONG(SqlType.LONG, SqlType.LONG, new Class<?>[] { long.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Long) results.getLong(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Long.parseLong(defaultStr);
		}
		@Override
		public Object convertIdNumber(Number number) {
			return (Long) number.longValue();
		}
		@Override
		public Number resultToId(DatabaseResults results, int columnPos) throws SQLException {
			return (Long) results.getLong(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
		@Override
		public boolean isPrimitive() {
			return true;
		}
	},

	/**
	 * Persists the {@link Long} Java class.
	 */
	LONG_OBJ(SqlType.LONG, null, new Class<?>[] { Long.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Long) results.getLong(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Long.parseLong(defaultStr);
		}
		@Override
		public Object convertIdNumber(Number number) {
			return (Long) number.longValue();
		}
		@Override
		public Number resultToId(DatabaseResults results, int columnPos) throws SQLException {
			return (Long) results.getLong(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Persists the float primitive.
	 */
	FLOAT(SqlType.FLOAT, null, new Class<?>[] { float.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Float) results.getFloat(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Float.parseFloat(defaultStr);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
		@Override
		public boolean isPrimitive() {
			return true;
		}
	},

	/**
	 * Persists the {@link Float} Java class.
	 */
	FLOAT_OBJ(SqlType.FLOAT, null, new Class<?>[] { Float.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Float) results.getFloat(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Float.parseFloat(defaultStr);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Persists the double primitive.
	 */
	DOUBLE(SqlType.DOUBLE, null, new Class<?>[] { double.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Double) results.getDouble(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Double.parseDouble(defaultStr);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
		@Override
		public boolean isPrimitive() {
			return true;
		}
	},

	/**
	 * Persists the {@link Double} Java class.
	 */
	DOUBLE_OBJ(SqlType.DOUBLE, null, new Class<?>[] { Double.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return (Double) results.getDouble(columnPos);
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Double.parseDouble(defaultStr);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Persists an unknown Java Object that is serializable.
	 */
	SERIALIZABLE(SqlType.SERIALIZABLE, null, new Class<?>[] { Object.class }) {
		@Override
		public Object javaToArg(FieldType fieldType, Object obj) throws SQLException {
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
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			throw new SQLException("Default values for serializable types are not supported");
		}
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			byte[] bytes = results.getBytes(columnPos);
			// need to do this check because we are a stream type
			if (bytes == null) {
				return null;
			}
			try {
				ObjectInputStream objInStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
				return objInStream.readObject();
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not read serialized object from byte array: "
						+ Arrays.toString(bytes) + "(len " + bytes.length + ")", e);
			}
		}
		@Override
		public boolean isValidForType(Class<?> fieldClass) {
			return Serializable.class.isAssignableFrom(fieldClass);
		}
		@Override
		public boolean isStreamType() {
			// can't do a getObject call beforehand so we have to check for nulls
			return true;
		}
	},

	/**
	 * Persists an Enum Java class as its string value. You can also specify the {@link #ENUM_INTEGER} as the type.
	 */
	ENUM_STRING(SqlType.STRING, null, new Class<?>[] { Enum.class }) {
		@Override
		public Object javaToArg(FieldType fieldType, Object obj) throws SQLException {
			Enum<?> enumVal = (Enum<?>) obj;
			return enumVal.name();
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return defaultStr;
		}
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			String val = results.getString(columnPos);
			return fieldType.enumFromString(val);
		}
	},

	/**
	 * Persists an Enum Java class as its ordinal interger value. You can also specify the {@link #ENUM_STRING} as the
	 * type.
	 */
	ENUM_INTEGER(SqlType.INTEGER, null, new Class<?>[] { Enum.class }) {
		@Override
		public Object javaToArg(FieldType fieldType, Object obj) throws SQLException {
			Enum<?> enumVal = (Enum<?>) obj;
			return (Integer) enumVal.ordinal();
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return Integer.parseInt(defaultStr);
		}
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return fieldType.enumFromInt(results.getInt(columnPos));
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Marker for fields that are unknown.
	 */
	UNKNOWN(SqlType.UNKNOWN, null, new Class<?>[0]) {
		@Override
		public Object javaToArg(FieldType fieldType, Object obj) throws SQLException {
			return null;
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return null;
		}
		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return null;
		}
	},
	// end
	;

	public static final String DEFAULT_DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSSSSS";

	private static Map<String, DateFormat> dateFormatMap;
	private final SqlType sqlType;
	private final SqlType convertForSqlType;
	private final boolean canBeGenerated;
	private final Class<?>[] classes;

	private DataType(SqlType sqlType, SqlType convertForSqlType, Class<?>[] classes) {
		this.sqlType = sqlType;
		this.convertForSqlType = convertForSqlType;
		// only types which have overridden the convertNumber method can be generated
		this.canBeGenerated = (convertIdNumber(10) != null);
		this.classes = classes;
	}

	public abstract Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos)
			throws SQLException;

	public abstract Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException;

	public Object javaToArg(FieldType fieldType, Object javaObject) throws SQLException {
		// noop pass-thru is the default
		return javaObject;
	}

	public SqlType getSqlType() {
		return sqlType;
	}

	/**
	 * Return true if this type can be auto-generated by the database. Probably only numbers will return true.
	 */
	public boolean isValidGeneratedType() {
		return canBeGenerated;
	}

	/**
	 * Convert a {@link Number} object to its primitive object suitable for assigning to an ID field.
	 */
	public Object convertIdNumber(Number number) {
		// by default the type cannot convert an id number
		return null;
	}

	/**
	 * Return the object suitable to be set on an id field that was extracted from the results associated with column in
	 * position columnPos.
	 */
	public Number resultToId(DatabaseResults results, int columnPos) throws SQLException {
		// by default the type cannot convert an id number
		return null;
	}

	/**
	 * Return true if the fieldClass is appropriate for this enum.
	 */
	public boolean isValidForType(Class<?> fieldClass) {
		// by default this is a noop
		return true;
	}

	/**
	 * Static method that returns the DataType associated with the class argument or {@link #UNKNOWN} if not found.
	 */
	public static DataType lookupClass(Class<?> dataClass) {
		for (DataType dataType : values()) {
			// build a static map from class to associated type
			for (Class<?> dataTypeClass : dataType.classes) {
				if (dataTypeClass == dataClass) {
					return dataType;
				}
			}
		}
		if (dataClass.isEnum()) {
			// special handling of the Enum type
			return ENUM_STRING;
		} else if (Serializable.class.isAssignableFrom(dataClass)) {
			// special handling of the serializable type
			return SERIALIZABLE;
		} else {
			return UNKNOWN;
		}
	}

	/**
	 * Return the DataType associated with the SqlType argument.
	 */
	public static DataType dataTypeFromSqlType(SqlType sqlType) {
		for (DataType dataType : values()) {
			if (dataType.convertForSqlType == sqlType) {
				return dataType;
			}
		}
		return null;
	}

	/**
	 * Return whether this field's default value should be escaped in SQL.
	 */
	public boolean isEscapeDefaultValue() {
		// default is to not escape the type if it is a number
		return !isNumber();
	}

	/**
	 * Return whether this field is a number.
	 */
	public boolean isNumber() {
		// can't determine this from the classes because primitives can't do assignable from
		return false;
	}

	/**
	 * Return whether this field is a primitive type or not.
	 */
	public boolean isPrimitive() {
		return false;
	}

	public boolean isStreamType() {
		return false;
	}

	private static synchronized Date parseDateString(String format, String dateStr) throws ParseException {
		DateFormat dateFormat = getDateFormat(format);
		return dateFormat.parse(dateStr);
	}

	private static synchronized String normalizeDateString(String format, String dateStr) throws ParseException {
		DateFormat dateFormat = getDateFormat(format);
		Date date = dateFormat.parse(dateStr);
		return dateFormat.format(date);
	}

	private static synchronized String formatDate(String format, Date date) {
		DateFormat dateFormat = getDateFormat(format);
		return dateFormat.format(date);
	}

	/**
	 * Return the date format for the format string.
	 * 
	 * NOTE: We should already be synchronized here.
	 */
	private static DateFormat getDateFormat(String formatStr) {
		if (dateFormatMap == null) {
			dateFormatMap = new HashMap<String, DateFormat>();
		}
		formatStr = formatOrDefault(formatStr);
		DateFormat dateFormat = dateFormatMap.get(formatStr);
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat(formatStr);
			dateFormatMap.put(formatStr, dateFormat);
		}
		return dateFormat;
	}

	private static String formatOrDefault(String format) {
		if (format == null) {
			return DEFAULT_DATE_FORMAT_STRING;
		} else {
			return format;
		}
	}
}
