package com.j256.ormlite.field;

import java.sql.Types;

/**
 * The SQL data types that are supported. These are basically an enumeration of the constants in {@link Types}.
 * 
 * @author graywatson
 */
public enum SqlType {

	STRING(Types.VARCHAR),
	DATE(Types.TIMESTAMP),
	BOOLEAN(Types.BOOLEAN),
	BYTE(Types.TINYINT),
	SHORT(Types.SMALLINT),
	INTEGER(Types.INTEGER),
	LONG(Types.BIGINT, Types.DECIMAL, Types.NUMERIC),
	FLOAT(Types.FLOAT),
	DOUBLE(Types.DOUBLE),
	SERIALIZABLE(Types.VARBINARY),
	// the following do not need to be handled except in specific situations
	BLOB(Types.BLOB),
	UNKNOWN(0),
	// end
	;

	private final int sqlType;
	private final int[] conversionTypeVals;

	private SqlType(int sqlType, int... conversionTypeVals) {
		this.sqlType = sqlType;
		this.conversionTypeVals = conversionTypeVals;
	}

	public int getTypeVal() {
		return sqlType;
	}

	public int[] getConversionTypeVals() {
		return conversionTypeVals;
	}
}
