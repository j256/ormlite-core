package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;

/**
 * Base class for all of the {@link java.sql.Date} class types.
 * 
 * @author graywatson
 */
public abstract class BaseDateType extends BaseDataType {

	private static final DateStringFormatConfig DEFAULT_DATE_FORMAT_CONFIG =
			new DateStringFormatConfig("yyyy-MM-dd HH:mm:ss.SSSSSS");
	private static final DateStringFormatConfig NO_MILLIS_DATE_FORMAT_CONFIG =
			new DateStringFormatConfig("yyyy-MM-dd HH:mm:ss");

	protected BaseDateType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	protected BaseDateType(SqlType sqlType) {
		super(sqlType);
	}

	protected static DateStringFormatConfig convertDateStringConfig(FieldType fieldType,
			DateStringFormatConfig defaultDateFormatConfig) {
		if (fieldType == null) {
			return defaultDateFormatConfig;
		}
		DateStringFormatConfig configObj = (DateStringFormatConfig) fieldType.getDataTypeConfigObj();
		if (configObj == null) {
			return defaultDateFormatConfig;
		} else {
			return (DateStringFormatConfig) configObj;
		}
	}

	protected static Date parseDateString(DateStringFormatConfig formatConfig, String dateStr) throws ParseException {
		DateFormat dateFormat = conditionalFormat(formatConfig, dateStr);
		return dateFormat.parse(dateStr);
	}

	protected static String normalizeDateString(DateStringFormatConfig formatConfig, String dateStr)
			throws ParseException {
		DateFormat dateFormat = conditionalFormat(formatConfig, dateStr);
		Date date = dateFormat.parse(dateStr);
		return dateFormat.format(date);
	}

	@Override
	public boolean isValidForVersion() {
		return true;
	}

	@Override
	public Object moveToNextValue(Object currentValue) {
		long newVal = System.currentTimeMillis();
		if (currentValue == null) {
			return new Date(newVal);
		} else if (newVal == ((Date) currentValue).getTime()) {
			return new Date(newVal + 1L);
		} else {
			return new Date(newVal);
		}
	}

	@Override
	public boolean isValidForField(Field field) {
		return (field.getType() == Date.class);
	}

	/**
	 * Get the default date format configuration.
	 */
	protected DateStringFormatConfig getDefaultDateFormatConfig() {
		return DEFAULT_DATE_FORMAT_CONFIG;
	}

	/**
	 * Bit of a hack here. If they aren't specifying a custom format then we check the date-string to see if it has a
	 * period. If it has no period then we switch to the no-millis pattern. This is necessary because most databases
	 * support the .SSSSSS format but H2 dropped the millis because of SQL compliance in 1.4.something.
	 */
	private static DateFormat conditionalFormat(DateStringFormatConfig formatConfig, String dateStr) {
		if (formatConfig == DEFAULT_DATE_FORMAT_CONFIG && dateStr.indexOf('.') < 0) {
			return NO_MILLIS_DATE_FORMAT_CONFIG.getDateFormat();
		} else {
			return formatConfig.getDateFormat();
		}
	}
}
