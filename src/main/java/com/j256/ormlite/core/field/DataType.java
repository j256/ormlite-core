package com.j256.ormlite.core.field;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.j256.ormlite.core.field.types.BigDecimalNumericType;
import com.j256.ormlite.core.field.types.BigDecimalStringType;
import com.j256.ormlite.core.field.types.BigIntegerType;
import com.j256.ormlite.core.field.types.BooleanCharType;
import com.j256.ormlite.core.field.types.BooleanIntegerType;
import com.j256.ormlite.core.field.types.BooleanObjectType;
import com.j256.ormlite.core.field.types.BooleanType;
import com.j256.ormlite.core.field.types.ByteArrayType;
import com.j256.ormlite.core.field.types.ByteObjectType;
import com.j256.ormlite.core.field.types.ByteType;
import com.j256.ormlite.core.field.types.CharType;
import com.j256.ormlite.core.field.types.CharacterObjectType;
import com.j256.ormlite.core.field.types.DateLongType;
import com.j256.ormlite.core.field.types.DateIntegerType;
import com.j256.ormlite.core.field.types.DateStringType;
import com.j256.ormlite.core.field.types.DateTimeType;
import com.j256.ormlite.core.field.types.DateType;
import com.j256.ormlite.core.field.types.DoubleObjectType;
import com.j256.ormlite.core.field.types.DoubleType;
import com.j256.ormlite.core.field.types.EnumIntegerType;
import com.j256.ormlite.core.field.types.EnumStringType;
import com.j256.ormlite.core.field.types.EnumToStringType;
import com.j256.ormlite.core.field.types.FloatObjectType;
import com.j256.ormlite.core.field.types.FloatType;
import com.j256.ormlite.core.field.types.IntType;
import com.j256.ormlite.core.field.types.IntegerObjectType;
import com.j256.ormlite.core.field.types.LongObjectType;
import com.j256.ormlite.core.field.types.LongStringType;
import com.j256.ormlite.core.field.types.LongType;
import com.j256.ormlite.core.field.types.NativeUuidType;
import com.j256.ormlite.core.field.types.SerializableType;
import com.j256.ormlite.core.field.types.ShortObjectType;
import com.j256.ormlite.core.field.types.ShortType;
import com.j256.ormlite.core.field.types.SqlDateType;
import com.j256.ormlite.core.field.types.StringBytesType;
import com.j256.ormlite.core.field.types.StringType;
import com.j256.ormlite.core.field.types.TimeStampType;
import com.j256.ormlite.core.field.types.UuidType;

/**
 * Data type enumeration that corresponds to a {@link DataPersister}.
 * 
 * @author graywatson
 */
public enum DataType {

	/**
	 * Persists the {@link String} Java class.
	 */
	STRING(StringType.getSingleton()),
	/**
	 * Persists the {@link String} Java class.
	 */
	LONG_STRING(LongStringType.getSingleton()),
	/**
	 * Persists the {@link String} Java class as an array of bytes. By default this will use {@link #STRING} so you will
	 * need to specify this using {@link DatabaseField#dataType()}.
	 */
	STRING_BYTES(StringBytesType.getSingleton()),
	/**
	 * Persists the boolean Java primitive.
	 */
	BOOLEAN(BooleanType.getSingleton()),
	/**
	 * Persists the {@link Boolean} object Java class.
	 */
	BOOLEAN_OBJ(BooleanObjectType.getSingleton()),
	/**
	 * Persists the boolean Java primitive as a character in the database.
	 */
	BOOLEAN_CHAR(BooleanCharType.getSingleton()),
	/**
	 * Persists the boolean Java primitive as an integer in the database.
	 */
	BOOLEAN_INTEGER(BooleanIntegerType.getSingleton()),
	/**
	 * Persists the {@link java.util.Date} Java class.
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 */
	DATE(DateType.getSingleton()),

	/**
	 * Persists the {@link java.util.Date} Java class as long milliseconds since epoch. By default this will use
	 * {@link #DATE} so you will need to specify this using {@link DatabaseField#dataType()}.
	 * 
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 */
	DATE_LONG(DateLongType.getSingleton()),

	/**
	 * Persists the {@link java.util.Date} Java class as int seconds since epoch. By default this will use {@link #DATE}
	 * so you will need to specify this using {@link DatabaseField#dataType()}.
	 *
	 * <p>
	 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
	 * </p>
	 */
	DATE_INTEGER(DateIntegerType.getSingleton()),
	/**
	 * Persists the {@link java.util.Date} Java class as a string of a format. By default this will use {@link #DATE} so
	 * you will need to specify this using {@link DatabaseField#dataType()}.
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
	DATE_STRING(DateStringType.getSingleton()),
	/**
	 * Persists the char primitive.
	 */
	CHAR(CharType.getSingleton()),
	/**
	 * Persists the {@link Character} object Java class.
	 */
	CHAR_OBJ(CharacterObjectType.getSingleton()),
	/**
	 * Persists the byte primitive.
	 */
	BYTE(ByteType.getSingleton()),
	/**
	 * Persists the byte[] array type. Because of some backwards compatibility issues, you will need to specify this
	 * using {@link DatabaseField#dataType()}. It won't be detected automatically.
	 */
	BYTE_ARRAY(ByteArrayType.getSingleton()),
	/**
	 * Persists the {@link Byte} object Java class.
	 */
	BYTE_OBJ(ByteObjectType.getSingleton()),
	/**
	 * Persists the short primitive.
	 */
	SHORT(ShortType.getSingleton()),
	/**
	 * Persists the {@link Short} object Java class.
	 */
	SHORT_OBJ(ShortObjectType.getSingleton()),
	/**
	 * Persists the int primitive.
	 */
	INTEGER(IntType.getSingleton()),
	/**
	 * Persists the {@link Integer} object Java class.
	 */
	INTEGER_OBJ(IntegerObjectType.getSingleton()),
	/**
	 * Persists the long primitive.
	 */
	LONG(LongType.getSingleton()),
	/**
	 * Persists the {@link Long} object Java class.
	 */
	LONG_OBJ(LongObjectType.getSingleton()),
	/**
	 * Persists the float primitive.
	 */
	FLOAT(FloatType.getSingleton()),
	/**
	 * Persists the {@link Float} object Java class.
	 */
	FLOAT_OBJ(FloatObjectType.getSingleton()),
	/**
	 * Persists the double primitive.
	 */
	DOUBLE(DoubleType.getSingleton()),
	/**
	 * Persists the {@link Double} object Java class.
	 */
	DOUBLE_OBJ(DoubleObjectType.getSingleton()),
	/**
	 * Persists an unknown Java Object that is serializable. Because of some backwards and forwards compatibility
	 * concerns, you will need to specify this using {@link DatabaseField#dataType()}. It won't be detected
	 * automatically.
	 */
	SERIALIZABLE(SerializableType.getSingleton()),
	/**
	 * See {@link #ENUM_NAME}
	 */
	ENUM_STRING(EnumStringType.getSingleton()),
	/**
	 * Persists an Enum Java class as its name produced by call @{link {@link Enum#name()}. You can also specify the
	 * {@link #ENUM_INTEGER} or {@link #ENUM_TO_STRING} as the type.
	 */
	ENUM_NAME(EnumStringType.getSingleton()),
	/**
	 * Persists an Enum Java class as its string value produced by call @{link {@link Enum#toString()}. You can also
	 * specify the {@link #ENUM_INTEGER} or {@link #ENUM_STRING} as the type.
	 */
	ENUM_TO_STRING(EnumToStringType.getSingleton()),
	/**
	 * Persists an Enum Java class as its ordinal integer value. You can also specify the {@link #ENUM_STRING} or
	 * {@link #ENUM_TO_STRING} as the type.
	 */
	ENUM_INTEGER(EnumIntegerType.getSingleton()),
	/**
	 * Persists the {@link java.util.UUID} Java class.
	 */
	UUID(UuidType.getSingleton()),
	/**
	 * Persists the {@link java.util.UUID} Java class as a native UUID column which is only supported by a couple of
	 * database types.
	 */
	UUID_NATIVE(NativeUuidType.getSingleton()),
	/**
	 * Persists the {@link BigInteger} Java class.
	 */
	BIG_INTEGER(BigIntegerType.getSingleton()),
	/**
	 * Persists the {@link BigDecimal} Java class as a String.
	 */
	BIG_DECIMAL(BigDecimalStringType.getSingleton()),
	/**
	 * Persists the {@link BigDecimal} Java class as a SQL NUMERIC.
	 */
	BIG_DECIMAL_NUMERIC(BigDecimalNumericType.getSingleton()),
	/**
	 * Persists the org.joda.time.DateTime type as a long integer. This uses reflection since we don't want to add the
	 * dependency. Because of this, you have to specify this using {@link DatabaseField#dataType()}. It won't be
	 * detected automatically.
	 */
	DATE_TIME(DateTimeType.getSingleton()),
	/**
	 * Persists the {@link java.sql.Date} Java class.
	 * 
	 * <p>
	 * NOTE: If you want to use the {@link java.util.Date} class then use {@link #DATE} which is recommended instead.
	 * </p>
	 */
	SQL_DATE(SqlDateType.getSingleton()),
	/**
	 * Persists the {@link java.sql.Timestamp} Java class. The {@link #DATE} type is recommended instead.
	 */
	TIME_STAMP(TimeStampType.getSingleton()),
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
