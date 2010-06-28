package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;

import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.JdbcTemplate;
import com.j256.ormlite.table.TableInfo;

public class MappedDeleteCollectionTest extends BaseOrmLiteTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNoIdBuildDelete() throws Exception {
		JdbcTemplate jdbcTemplate = createMock(JdbcTemplate.class);
		MappedDeleteCollection.deleteObjects(databaseType, new TableInfo<NoId>(databaseType, NoId.class), jdbcTemplate,
				new ArrayList<NoId>());
	}

	protected static class NoId {
		@DatabaseField
		String stuff;
	}
}
