package com.j256.ormlite.field.types;

import org.junit.Test;

import com.j256.ormlite.field.SqlType;

public class BigDecimalStringTypeTest {

	@Test
	public void testCoverage() {
		new BigDecimalStringType(SqlType.STRING, new Class[0]);
	}
}
