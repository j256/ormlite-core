package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;

import org.junit.Test;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.BaseCoreStmtTest;
import com.j256.ormlite.support.DatabaseResults;

public class BaseMappedQueryTest extends BaseCoreStmtTest {

	@Test
	public void testMappedQuery() throws Exception {
		Field field = Foo.class.getDeclaredField(Foo.ID_COLUMN_NAME);
		String tableName = "basefoo";
		FieldType[] resultFieldTypes =
				new FieldType[] { FieldType.createFieldType(connectionSource, tableName, field, Foo.class) };
		BaseMappedQuery<Foo, Integer> baseMappedQuery =
				new BaseMappedQuery<Foo, Integer>(baseFooTableInfo, "select * from " + tableName, new FieldType[0],
						resultFieldTypes) {
				};
		DatabaseResults results = createMock(DatabaseResults.class);
		int colN = 1;
		expect(results.getObjectCache()).andReturn(null);
		expect(results.findColumn(Foo.ID_COLUMN_NAME)).andReturn(colN);
		int id = 63365;
		expect(results.getInt(colN)).andReturn(id);
		replay(results);
		Foo baseFoo = baseMappedQuery.mapRow(results);
		assertNotNull(baseFoo);
		assertEquals(id, baseFoo.id);
		verify(results);
	}
}
