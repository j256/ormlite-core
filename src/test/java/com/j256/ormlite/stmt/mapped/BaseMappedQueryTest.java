package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.DatabaseResults;

public class BaseMappedQueryTest extends BaseCoreTest {

	@Test
	public void testMappedQuery() throws Exception {
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		List<FieldType> resultFieldTypeList = new ArrayList<FieldType>();
		Field field = BaseFoo.class.getDeclaredField(BaseFoo.ID_COLUMN_NAME);
		String tableName = "basefoo";
		resultFieldTypeList.add(FieldType.createFieldType(connectionSource, tableName, field, 0));
		BaseMappedQuery<BaseFoo, String> baseMappedQuery =
				new BaseMappedQuery<BaseFoo, String>(baseFooTableInfo, "select * from " + tableName, argFieldTypeList,
						resultFieldTypeList) {
				};
		DatabaseResults results = createMock(DatabaseResults.class);
		int colN = 1;
		expect(results.findColumn(BaseFoo.ID_COLUMN_NAME)).andReturn(colN);
		expect(results.isNull(colN)).andReturn(false);
		String idString = "deopdjed";
		expect(results.getString(colN)).andReturn(idString);
		replay(results);
		BaseFoo baseFoo = baseMappedQuery.mapRow(results);
		assertNotNull(baseFoo);
		assertEquals(idString, baseFoo.id);
		verify(results);
	}
}
