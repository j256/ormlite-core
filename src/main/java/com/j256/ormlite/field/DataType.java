package com.j256.ormlite.field;

import com.j256.ormlite.field.types.BooleanObjectType;
import com.j256.ormlite.field.types.BooleanType;
import com.j256.ormlite.field.types.ByteArrayType;
import com.j256.ormlite.field.types.ByteObjectType;
import com.j256.ormlite.field.types.ByteType;
import com.j256.ormlite.field.types.CharType;
import com.j256.ormlite.field.types.CharacterObjectType;
import com.j256.ormlite.field.types.DateLongType;
import com.j256.ormlite.field.types.DateStringType;
import com.j256.ormlite.field.types.DateType;
import com.j256.ormlite.field.types.DoubleObjectType;
import com.j256.ormlite.field.types.DoubleType;
import com.j256.ormlite.field.types.EnumIntegerType;
import com.j256.ormlite.field.types.EnumStringType;
import com.j256.ormlite.field.types.FloatObjectType;
import com.j256.ormlite.field.types.FloatType;
import com.j256.ormlite.field.types.IntType;
import com.j256.ormlite.field.types.IntegerObjectType;
import com.j256.ormlite.field.types.LongObjectType;
import com.j256.ormlite.field.types.LongStringType;
import com.j256.ormlite.field.types.LongType;
import com.j256.ormlite.field.types.SerializableType;
import com.j256.ormlite.field.types.ShortObjectType;
import com.j256.ormlite.field.types.ShortType;
import com.j256.ormlite.field.types.StringBytesType;
import com.j256.ormlite.field.types.StringType;
import com.j256.ormlite.field.types.UuidType;

/**
 * Data type enumeration that corresponds to a {@link DataPersister}.
 * 
 * @author graywatson
 */
public enum DataType {

	/**
	 * Persists the {@link String} Java class.
	 */
	STRING(StringType.createType()),
	/**
	 * Persists the {@link String} Java class.
	 */
	LONG_STRING(LongStringType.createType()),
	/**
	 * Persists the {@link String} Java class.
	 */
	STRING_BYTES(StringBytesType.createType()),
	/**
	 * Persists the boolean Java primitive.
	 */
	BOOLEAN(BooleanType.createType()),
	/**
	 * Persists the {@link Boolean} Java class.
	 */
	BOOLEAN_OBJ(BooleanObjectType.createType()),
	/**
	 * Persists the {@link java.util.Date} Java class.
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 */
	DATE(DateType.createType()),
	/**
	 * @deprecated You should use {@link DataType#DATE}
	 */
	@Deprecated
	JAVA_DATE(DATE.dataPersister),

	/**
	 * Persists the {@link java.util.Date} Java class as long milliseconds since epoch.
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 */
	DATE_LONG(DateLongType.createType()),
	/**
	 * @deprecated You should use {@link DataType#DATE_LONG}
	 */
	@Deprecated
	JAVA_DATE_LONG(DATE_LONG.dataPersister),
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
	DATE_STRING(DateStringType.createType()),
	/**
	 * @deprecated You should use {@link DataType#DATE_STRING}
	 */
	@Deprecated
	JAVA_DATE_STRING(DATE_STRING.dataPersister),
	/**
	 * Persists the char primitive.
	 */
	CHAR(CharType.createType()),
	/**
	 * Persists the char primitive.
	 */
	CHAR_OBJ(CharacterObjectType.createType()),
	/**
	 * Persists the byte primitive.
	 */
	BYTE(ByteType.createType()),
	/**
	 * Persists the byte[] array type.
	 */
	BYTE_ARRAY(ByteArrayType.createType()),
	/**
	 * Persists the {@link Byte} Java class.
	 */
	BYTE_OBJ(ByteObjectType.createType()),
	/**
	 * Persists the short primitive.
	 */
	SHORT(ShortType.createType()),
	/**
	 * Persists the {@link Short} Java class.
	 */
	SHORT_OBJ(ShortObjectType.createType()),
	/**
	 * Persists the int primitive.
	 */
	INTEGER(IntType.createType()),
	/**
	 * Persists the {@link Integer} Java class.
	 */
	INTEGER_OBJ(IntegerObjectType.createType()),
	/**
	 * Persists the long primitive.
	 */
	LONG(LongType.createType()),
	/**
	 * Persists the {@link Long} Java class.
	 */
	LONG_OBJ(LongObjectType.createType()),
	/**
	 * Persists the float primitive.
	 */
	FLOAT(FloatType.createType()),
	/**
	 * Persists the {@link Float} Java class.
	 */
	FLOAT_OBJ(FloatObjectType.createType()),
	/**
	 * Persists the double primitive.
	 */
	DOUBLE(DoubleType.createType()),
	/**
	 * Persists the {@link Double} Java class.
	 */
	DOUBLE_OBJ(DoubleObjectType.createType()),
	/**
	 * Persists an unknown Java Object that is serializable.
	 */
	SERIALIZABLE(SerializableType.createType()),
	/**
	 * Persists an Enum Java class as its string value. You can also specify the {@link #ENUM_INTEGER} as the type.
	 */
	ENUM_STRING(EnumStringType.createType()),
	/**
	 * Persists an Enum Java class as its ordinal integer value. You can also specify the {@link #ENUM_STRING} as the
	 * type.
	 */
	ENUM_INTEGER(EnumIntegerType.createType()),
	/**
	 * Persists the {@link java.util.UUID} Java class.
	 */
	UUID(UuidType.createType()),
	/**
	 * Marker for fields that are unknown.
	 */
	UNKNOWN(null),
	// end
	;

	private final DataPersister dataPersister;

	private DataType(DataPersister dataPersister) {
		this.dataPersister = dataPersister;
	}

	public DataPersister getDataPersister() {
		return dataPersister;
	}
}
