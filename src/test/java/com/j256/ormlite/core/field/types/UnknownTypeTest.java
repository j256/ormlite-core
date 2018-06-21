package com.j256.ormlite.core.field.types;

import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.ormlite.core.field.DataType;

public class UnknownTypeTest extends BaseTypeTest {

	@Test
	public void testUnknownGetResult() {
		DataType dataType = DataType.UNKNOWN;
		assertNull(dataType.getDataPersister());
	}
}
