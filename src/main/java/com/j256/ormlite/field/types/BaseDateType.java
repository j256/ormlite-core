package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;

/**
 * Base class for all of the {@link java.sql.Date} class types.
 * 
 * @author graywatson
 */
public abstract class BaseDateType extends BaseDataType {

	protected static final DateStringFormatConfig defaultDateFormatConfig = new DateStringFormatConfig(
			"yyyy-MM-dd HH:mm:ss.SSS Z", "yyyy-MM-dd HH:mm:ss.SSSSSS");

	protected BaseDateType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
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
		return formatConfig.parse(dateStr);
	}

	protected static String normalizeDateString(DateStringFormatConfig formatConfig, String dateStr)
			throws ParseException {
		Date date = formatConfig.parse(dateStr);
		return formatConfig.format(date);
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
	 * Date string format configuration information.
	 */
	protected static class DateStringFormatConfig {
		final String[] dateFormatStrings;
		private final ThreadLocal<DateSizeFormats> threadLocal = new ThreadLocal<DateSizeFormats>() {
			@Override
			protected DateSizeFormats initialValue() {
				return new DateSizeFormats(dateFormatStrings);
			}
		};
		/**
		 * Handles a number of different formats. The first one is the default. The rest are used as parsing
		 * alternatives.
		 */
		public DateStringFormatConfig(String... dateFormatStrings) {
			this.dateFormatStrings = dateFormatStrings;
		}
		public Date parse(String dateStr) throws ParseException {
			return threadLocal.get().parse(dateStr);
		}
		public String format(Date date) {
			return threadLocal.get().format(date);
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String format : dateFormatStrings) {
				if (first) {
					first = false;
				} else {
					sb.append(" or ");
				}
				sb.append(format);
			}
			return sb.toString();
		}
	}

	/**
	 * Class stored in a thread-local that holds a number of date-formats to try to parse a date-string.
	 */
	private static class DateSizeFormats {
		final DateFormat[] formats;
		/**
		 * The first format string is considered the _default_. The rest are used as parsing alternatives.
		 */
		public DateSizeFormats(String[] dateFormatStrings) {
			this.formats = new DateFormat[dateFormatStrings.length];
			for (int i = 0; i < dateFormatStrings.length; i++) {
				this.formats[i] = new SimpleDateFormat(dateFormatStrings[i]);
			}
		}
		public Date parse(String dateString) throws ParseException {
			ParseException parseException = null;
			for (DateFormat format : formats) {
				try {
					return format.parse(dateString);
				} catch (ParseException e) {
					parseException = e;
				}
			}
			throw parseException;
		}
		/**
		 * Format a date using the default format.
		 */
		public String format(Date date) {
			return formats[0].format(date);
		}
	}
}
