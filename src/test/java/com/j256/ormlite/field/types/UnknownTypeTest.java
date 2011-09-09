package com.j256.ormlite.field.types;

import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class UnknownTypeTest extends BaseTypeTest {

	@Test
	public void testUnknownGetResult() throws Exception {
		DataType dataType = DataType.UNKNOWN;
		assertNull(dataType.getDataPersister());
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalUnknown {
		@DatabaseField
		LocalUnknown unkown;
	}
}
