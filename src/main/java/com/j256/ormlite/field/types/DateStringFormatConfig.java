package com.j256.ormlite.field.types;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Date string format config that is it's own class to force the hiding of the DateFormat.
 * 
 * @author graywatson
 */
public class DateStringFormatConfig {

	private final String dateFormatStr;
	// used with clone
	private final DateFormat dateFormat;

	public DateStringFormatConfig(String dateFormatStr) {
		this.dateFormatStr = dateFormatStr;
		this.dateFormat = new SimpleDateFormat(dateFormatStr);
	}

	public DateFormat getDateFormat() {
		return (DateFormat) dateFormat.clone();
	}

	@Override
	public String toString() {
		return dateFormatStr;
	}
}
