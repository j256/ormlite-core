package com.j256.ormlite.field.types;

import java.sql.SQLException;
import java.util.Date;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Persists the {@link java.util.Date} Java class as long milliseconds since epoch.
 * 
 * <p>
 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
 * </p>
 * 
 * @author graywatson
 */
public class DateLongType extends BaseDataType {

	private static final DateLongType singleTon = new DateLongType();

	public static DateLongType getSingleton() {
		return singleTon;
	}

	private DateLongType() {
		super(SqlType.LONG, new Class<?>[0]);
	}

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
	public Object javaToSqlArg(FieldType fieldType, Object obj) {
		Date date = (Date) obj;
		return (Long) date.getTime();
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}
}
