package com.j256.ormlite.field.types;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Type that persists a {@link java.util.Date} object as a String.
 * 
 * @author graywatson
 */
public class DateStringType extends BaseDateType {

	public static int DEFAULT_WIDTH = 50;

	private static final DateStringType singleTon = new DateStringType();

	public static DateStringType createType() {
		return singleTon;
	}

	private DateStringType() {
		super(SqlType.STRING, new Class<?>[0]);
	}

	@Override
	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		String dateStr = results.getString(columnPos);
		if (dateStr == null) {
			return null;
		}
		DateStringFormatConfig formatConfig = convertDateStringConfig(fieldType);
		try {
			return parseDateString(formatConfig, dateStr);
		} catch (ParseException e) {
			throw SqlExceptionUtil.create("Problems with column " + columnPos + " parsing date-string '" + dateStr
					+ "' using '" + formatConfig + "'", e);
		}
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		DateStringFormatConfig formatConfig = convertDateStringConfig(fieldType);
		try {
			// we parse to make sure it works and then format it again
			return normalizeDateString(formatConfig, defaultStr);
		} catch (ParseException e) {
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default date-string '"
					+ defaultStr + "' using '" + formatConfig + "'", e);
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) {
		Date date = (Date) obj;
		return formatDate(convertDateStringConfig(fieldType), date);
	}

	@Override
	public Object makeConfigObject(FieldType fieldType) {
		String format = fieldType.getFormat();
		if (format == null) {
			return defaultDateFormatConfig;
		} else {
			return new DateStringFormatConfig(format);
		}
	}

	@Override
	public int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}
}
