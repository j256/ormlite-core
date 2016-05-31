package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.QueryBuilder;

public class RawRowObjectMapperTest extends BaseCoreTest {

	@Test
	public void testRawResultsObjectMapper() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 12321;
		foo1.stringField = "fjpojefpwoewfjpewf";
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 754282321;
		foo2.stringField = "foewjfewpfjwe";
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, Object> qb = dao.queryBuilder();
		qb.selectColumns(Foo.ID_COLUMN_NAME, Foo.VAL_COLUMN_NAME, Foo.STRING_COLUMN_NAME);
		GenericRawResults<Foo> rawResults =
				dao.queryRaw(qb.prepareStatementString(), new DataType[] { DataType.INTEGER, DataType.INTEGER,
						DataType.STRING }, new FooObjectArrayMapper());
		List<Foo> results = rawResults.getResults();
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(foo1.val, results.get(0).val);
		assertEquals(foo1.stringField, results.get(0).stringField);
		assertEquals(foo2.id, results.get(1).id);
		assertEquals(foo2.val, results.get(1).val);
		assertEquals(foo2.stringField, results.get(1).stringField);
	}

	private static class FooObjectArrayMapper implements RawRowObjectMapper<Foo> {
		@Override
		public Foo mapRow(String[] columnNames, DataType[] dataTypes, Object[] resultColumns) {
			// may be more than 1 because of the id
			assertTrue(resultColumns.length >= 2);
			// id is added at the end always
			Foo foo = new Foo();
			foo.id = (Integer) resultColumns[0];
			foo.val = (Integer) resultColumns[1];
			foo.stringField = (String) resultColumns[2];
			// other fields could be converted here
			return foo;
		}
	}
}
