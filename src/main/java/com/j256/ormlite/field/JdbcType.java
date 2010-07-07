package com.j256.ormlite.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.misc.SqlExceptionUtil;

/**
 * JDBC type enumeration to provide Java class to/from JDBC mapping.
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
public enum JdbcType implements FieldConverter {

	/**
	 * Links the {@link Types#VARCHAR} SQL type and the {@link String} Java class.
	 */
	STRING(Types.VARCHAR, new int[0], new Class<?>[] { String.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			return resultSet.getString(columnPos);
		}
	},

	/**
	 * Links the {@link Types#BOOLEAN} SQL type and the boolean primitive.
	 */
	BOOLEAN(Types.BOOLEAN, new int[0], new Class<?>[] { boolean.class, }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			checkPrimitiveValue(fieldType, resultSet, columnPos);
			return (Boolean) resultSet.getBoolean(columnPos);
		}
	},

	/**
	 * Links the {@link Types#BOOLEAN} SQL type and the {@link Boolean} Java class.
	 */
	BOOLEAN_OBJ(Types.BOOLEAN, new int[0], new Class<?>[] { Boolean.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			Object obj = resultSet.getObject(columnPos);
			if (obj == null) {
				return null;
			} else {
				return (Boolean) resultSet.getBoolean(columnPos);
			}
		}
	},

	/**
	 * Links the {@link Types#TIMESTAMP} SQL type and the {@link java.util.Date} Java class.
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 */
	JAVA_DATE(Types.TIMESTAMP, new int[0], new Class<?>[] { Date.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			Timestamp timeStamp = resultSet.getTimestamp(columnPos);
			if (timeStamp == null) {
				return null;
			} else {
				return new Date(timeStamp.getTime());
			}
		}
		@Override
		public Object javaToArg(Object javaObject) {
			Date date = (Date) javaObject;
			return new Timestamp(date.getTime());
		}
	},

	/**
	 * Links the {@link Types#TINYINT} SQL type and the byte primitive.
	 */
	BYTE(Types.TINYINT, new int[0], new Class<?>[] { byte.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			checkPrimitiveValue(fieldType, resultSet, columnPos);
			return (Byte) resultSet.getByte(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#TINYINT} SQL type and the {@link Byte} Java class.
	 */
	BYTE_OBJ(Types.TINYINT, new int[0], new Class<?>[] { Byte.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			Object obj = resultSet.getObject(columnPos);
			if (obj == null) {
				return null;
			} else {
				return (Byte) resultSet.getByte(columnPos);
			}
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#SMALLINT} SQL type and the short primitive.
	 */
	SHORT(Types.SMALLINT, new int[0], new Class<?>[] { short.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			checkPrimitiveValue(fieldType, resultSet, columnPos);
			return (Short) resultSet.getShort(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#SMALLINT} SQL type and the {@link Short} Java class.
	 */
	SHORT_OBJ(Types.SMALLINT, new int[0], new Class<?>[] { Short.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			Object obj = resultSet.getObject(columnPos);
			if (obj == null) {
				return null;
			} else {
				return (Short) resultSet.getShort(columnPos);
			}
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#INTEGER} SQL type and the int primitive.
	 */
	INTEGER(Types.INTEGER, new int[0], new Class<?>[] { int.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			checkPrimitiveValue(fieldType, resultSet, columnPos);
			return resultSet.getInt(columnPos);
		}
		@Override
		public Object convertIdNumber(Number number) {
			return (Integer) number.intValue();
		}
		@Override
		public Number resultToId(ResultSet resultSet, int columnPos) throws SQLException {
			return (Integer) resultSet.getInt(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#INTEGER} SQL type and the {@link Integer} Java class.
	 */
	INTEGER_OBJ(Types.INTEGER, new int[0], new Class<?>[] { Integer.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			Object obj = resultSet.getObject(columnPos);
			if (obj == null) {
				return null;
			} else {
				return (Integer) resultSet.getInt(columnPos);
			}
		}
		@Override
		public Object convertIdNumber(Number number) {
			return (Integer) number.intValue();
		}
		@Override
		public Number resultToId(ResultSet resultSet, int columnPos) throws SQLException {
			return (Integer) resultSet.getInt(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#BIGINT} SQL type and the long primitive.
	 */
	LONG(Types.BIGINT, new int[] { Types.DECIMAL, Types.NUMERIC }, new Class<?>[] { long.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			checkPrimitiveValue(fieldType, resultSet, columnPos);
			return (Long) resultSet.getLong(columnPos);
		}
		@Override
		public Object convertIdNumber(Number number) {
			return (Long) number.longValue();
		}
		@Override
		public Number resultToId(ResultSet resultSet, int columnPos) throws SQLException {
			return (Long) resultSet.getLong(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#BIGINT} SQL type and the {@link Long} Java class.
	 */
	LONG_OBJ(Types.BIGINT, new int[] { Types.DECIMAL, Types.NUMERIC }, new Class<?>[] { Long.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			Object obj = resultSet.getObject(columnPos);
			if (obj == null) {
				return null;
			} else {
				return (Long) resultSet.getLong(columnPos);
			}
		}
		@Override
		public Object convertIdNumber(Number number) {
			return (Long) number.longValue();
		}
		@Override
		public Number resultToId(ResultSet resultSet, int columnPos) throws SQLException {
			return (Long) resultSet.getLong(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#FLOAT} SQL type and the float primitive.
	 */
	FLOAT(Types.FLOAT, new int[0], new Class<?>[] { float.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			checkPrimitiveValue(fieldType, resultSet, columnPos);
			return (Float) resultSet.getFloat(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#FLOAT} SQL type and the {@link Float} Java class.
	 */
	FLOAT_OBJ(Types.FLOAT, new int[0], new Class<?>[] { Float.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			Object obj = resultSet.getObject(columnPos);
			if (obj == null) {
				return null;
			} else {
				return (Float) resultSet.getFloat(columnPos);
			}
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#DOUBLE} SQL type and the double primitive.
	 */
	DOUBLE(Types.DOUBLE, new int[0], new Class<?>[] { double.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			checkPrimitiveValue(fieldType, resultSet, columnPos);
			return (Double) resultSet.getDouble(columnPos);
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#DOUBLE} SQL type and the {@link Double} Java class.
	 */
	DOUBLE_OBJ(Types.DOUBLE, new int[0], new Class<?>[] { Double.class }) {
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			Object obj = resultSet.getObject(columnPos);
			if (obj == null) {
				return null;
			} else {
				return (Double) resultSet.getDouble(columnPos);
			}
		}
		@Override
		public boolean isNumber() {
			return true;
		}
	},

	/**
	 * Links the {@link Types#VARBINARY} SQL type and an unknown Java Object that is serializable.
	 */
	SERIALIZABLE(Types.VARBINARY, new int[0], new Class<?>[] { Object.class }) {
		@Override
		public Object javaToArg(Object javaObject) throws SQLException {
			if (javaObject == null) {
				return null;
			}
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			try {
				ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
				objOutStream.writeObject(javaObject);
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not write serialized object to byte array", e);
			}
			return outStream.toByteArray();
		}
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			byte[] bytes = resultSet.getBytes(columnPos);
			if (bytes == null) {
				return null;
			}
			try {
				ObjectInputStream objInStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
				return objInStream.readObject();
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not read serialized object from byte array", e);
			}
		}
		@Override
		public boolean isValidForType(Class<?> fieldClass) {
			return Serializable.class.isAssignableFrom(fieldClass);
		}
	},

	/**
	 * Links the {@link Types#VARCHAR} SQL type and the Enum Java class. You can also specify the {@link #ENUM_INTEGER}
	 * as the JdbcType.
	 */
	ENUM_STRING(Types.VARCHAR, new int[0], new Class<?>[] { Enum.class }) {
		@Override
		public Object javaToArg(Object obj) throws SQLException {
			if (obj == null) {
				return null;
			} else {
				Enum<?> enumVal = (Enum<?>) obj;
				return enumVal.name();
			}
		}
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			String val = resultSet.getString(columnPos);
			if (val == null) {
				return null;
			} else {
				return fieldType.enumFromString(val);
			}
		}
	},

	/**
	 * Links the {@link Types#INTEGER} SQL type and the Enum Java class. You can also specify the {@link #ENUM_STRING}
	 * as the JdbcType.
	 */
	ENUM_INTEGER(Types.INTEGER, new int[0], new Class<?>[] { Enum.class }) {
		@Override
		public Object javaToArg(Object obj) throws SQLException {
			if (obj == null) {
				return null;
			} else {
				Enum<?> enumVal = (Enum<?>) obj;
				return (Integer) enumVal.ordinal();
			}
		}
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			Object obj = resultSet.getObject(columnPos);
			if (obj == null) {
				return null;
			} else {
				return fieldType.enumFromInt(resultSet.getInt(columnPos));
			}
		}
	},

	/**
	 * Marker for fields that are unknown.
	 */
	UNKNOWN(0, new int[0], new Class<?>[0]) {
		@Override
		public Object javaToArg(Object obj) throws SQLException {
			return null;
		}
		@Override
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException {
			return null;
		}
	},
	// end
	;

	private static final Map<Class<?>, JdbcType> classMap = new HashMap<Class<?>, JdbcType>();
	private static final Map<Integer, JdbcType> idTypeMap = new HashMap<Integer, JdbcType>();

	static {
		for (JdbcType jdbcType : values()) {
			// build a static map from class to associated type
			for (Class<?> jdbcClass : jdbcType.classes) {
				classMap.put(jdbcClass, jdbcType);
			}
			// if it can be a generated-id, add to id type map
			if (jdbcType.isValidGeneratedType()) {
				idTypeMap.put(jdbcType.primaryTypeVal, jdbcType);
				for (int typeVal : jdbcType.convertTypeVals) {
					idTypeMap.put(typeVal, jdbcType);
				}
			}
		}
	}

	private final int primaryTypeVal;
	private final int[] convertTypeVals;
	private final boolean canBeGenerated;
	private final Class<?>[] classes;

	private JdbcType(int primaryTypeVal, int[] convertTypeVals, Class<?>[] classes) {
		this.primaryTypeVal = primaryTypeVal;
		this.convertTypeVals = convertTypeVals;
		// only types which have overridden the convertNumber method can be generated
		this.canBeGenerated = (convertIdNumber(10) != null);
		this.classes = classes;
	}

	public abstract Object resultToJava(FieldType fieldType, ResultSet resultSet, int columnPos) throws SQLException;

	public Object javaToArg(Object javaObject) throws SQLException {
		// noop pass-thru is the default
		return javaObject;
	}

	public int getJdbcTypeVal() {
		return primaryTypeVal;
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
	 * Return the object suitable to be set on an id field that was extracted from the resultSet associated with column
	 * in position columnPos.
	 */
	public Number resultToId(ResultSet resultSet, int columnPos) throws SQLException {
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
	 * Static method that returns the {@link JdbcType} associated with the class argument or {@link #UNKNOWN} if not
	 * found.
	 */
	public static JdbcType lookupClass(Class<?> dataClass) {
		JdbcType jdbcType = classMap.get(dataClass);
		if (jdbcType != null) {
			return jdbcType;
		} else if (dataClass.isEnum()) {
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
	 * Static method that returns the {@link JdbcType} associated with the SQL type value or {@link #UNKNOWN} if
	 * {@link #UNKNOWN} not found.
	 */
	public static JdbcType lookupIdTypeVal(int typeVal) {
		JdbcType jdbcType = idTypeMap.get(typeVal);
		if (jdbcType == null) {
			return UNKNOWN;
		} else {
			return jdbcType;
		}
	}

	public boolean isNumber() {
		// can't determine this from the classes because primitives can't do assignable from
		return false;
	}

	private static void checkPrimitiveValue(FieldType fieldType, ResultSet resultSet, int columnPos)
			throws SQLException {
		if (fieldType.isThrowIfNull() && resultSet.getObject(columnPos) == null) {
			throw new SQLException("Result set value for primitive field '" + fieldType.getFieldName()
					+ "' was an invalid null value");
		}
	}
}
