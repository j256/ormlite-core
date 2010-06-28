package com.j256.ormlite.stmt.mapped;

import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.TableInfo;

public class MappedUpdateIdTest extends BaseOrmLiteTest {

	@Test
	public void testUpdateIdNoId() throws Exception {
		assertNull(MappedUpdateId.build(databaseType, new TableInfo<NoId>(databaseType, NoId.class)));
	}

	protected static class NoId {
		@DatabaseField
		String id;
	}
}
