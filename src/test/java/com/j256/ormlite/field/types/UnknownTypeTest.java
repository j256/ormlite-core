package com.j256.ormlite.field.types;

import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.ormlite.field.DataType;

public class UnknownTypeTest extends BaseTypeTest {

	@Test
	public void testUnknownGetResult() {
		DataType dataType = DataType.UNKNOWN;
		assertNull(dataType.getDataPersister());
	}
}
