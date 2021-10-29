package com.j256.ormlite.field.types;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Date string format config that is it's own class to force the hiding of the DateFormat.
 * 
 * @author graywatson
 */
public class DateStringFormatConfig {

	private final String dateFormatStr;
	// used with clone
	private final DateFormat dateFormat;
	private final TimeZone timeZone;

	public DateStringFormatConfig(String dateFormatStr) {
		this(dateFormatStr, null);
	}

	public DateStringFormatConfig(String dateFormatStr, TimeZone timeZone) {
		this.dateFormatStr = dateFormatStr;
		this.dateFormat = new SimpleDateFormat(dateFormatStr);
		this.timeZone = timeZone;
	}

	public DateFormat getDateFormat() {
		DateFormat formatToUse = (DateFormat) dateFormat.clone();
		if (timeZone != null) {
			formatToUse.setTimeZone(timeZone);
		}
		return formatToUse;
	}

	@Override
	public String toString() {
		return dateFormatStr;
	}
}
