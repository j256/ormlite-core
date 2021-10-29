package com.j256.ormlite.field.types;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	// pattern which supports a {TZ XXX} prefix to the simple-date-format
	private static final Pattern FORMAT_PATTERN = Pattern.compile("([{]TZ\s+([^}]+)[}])?(.*)");

	private static final DateStringType singleTon = new DateStringType();

	public static DateStringType getSingleton() {
		return singleTon;
	}

	private DateStringType() {
		super(SqlType.STRING);
	}

	protected DateStringType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	protected DateStringType(SqlType sqlType) {
		super(sqlType);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		DateStringFormatConfig formatConfig = convertDateStringConfig(fieldType, getDefaultDateFormatConfig());
		try {
			// we parse to make sure it works and then format it again
			return normalizeDateString(formatConfig, defaultStr);
		} catch (ParseException e) {
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default date-string '"
					+ defaultStr + "' using '" + formatConfig + "'", e);
		}
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return results.getString(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
		String value = (String) sqlArg;
		DateStringFormatConfig formatConfig = convertDateStringConfig(fieldType, getDefaultDateFormatConfig());
		try {
			return parseDateString(formatConfig, value);
		} catch (ParseException e) {
			throw SqlExceptionUtil.create("Problems with column " + columnPos + " parsing date-string '" + value
					+ "' using '" + formatConfig + "'", e);
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) {
		DateFormat dateFormat = convertDateStringConfig(fieldType, getDefaultDateFormatConfig()).getDateFormat();
		return dateFormat.format((Date) obj);
	}

	@Override
	public Object makeConfigObject(FieldType fieldType) {
		String format = fieldType.getFormat();
		if (format == null) {
			return getDefaultDateFormatConfig();
		}
		TimeZone timeZone = null;
		Matcher matcher = FORMAT_PATTERN.matcher(format);
		if (matcher.matches()) {
			String zone = matcher.group(2);
			if (zone != null && !zone.isEmpty()) {
				// this returns the default if not found
				timeZone = TimeZone.getTimeZone(zone);
			}
			// the rest of the format is the simple-date-format
			format = matcher.group(3);
		}

		return new DateStringFormatConfig(format, timeZone);
	}

	@Override
	public int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) throws SQLException {
		return sqlArgToJava(fieldType, stringValue, columnPos);
	}

	@Override
	public Class<?> getPrimaryClass() {
		return byte[].class;
	}
}
