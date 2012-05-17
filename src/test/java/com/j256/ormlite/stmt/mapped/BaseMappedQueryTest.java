package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.BaseCoreStmtTest;
import com.j256.ormlite.stmt.QueryBuilder;
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

	@Test
	public void testInnerQueryCacheLookup() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		Dao<Outer, Object> outerDao = createDao(Outer.class, true);
		outerDao.setObjectCache(true);
		Dao<Inner, Object> innerDao = createDao(Inner.class, true);
		innerDao.setObjectCache(true);

		Foo foo = new Foo();
		assertEquals(1, fooDao.create(foo));

		Outer outer1 = new Outer();
		assertEquals(1, outerDao.create(outer1));
		Outer outer2 = new Outer();
		outer2.foreign = foo;
		assertEquals(1, outerDao.create(outer2));

		Inner inner = new Inner();
		inner.foreign = foo;
		assertEquals(1, innerDao.create(inner));

		QueryBuilder<Inner, Object> innerQb = innerDao.queryBuilder();
		innerQb.selectColumns(Inner.SOME_STRING_FIELD_NAME);
		QueryBuilder<Outer, Object> qb = outerDao.queryBuilder();
		List<Outer> results =
				qb.selectColumns(Outer.SOME_STRING_FIELD_NAME)
						.where()
						.in(Outer.SOME_STRING_FIELD_NAME, innerQb)
						.query();
		assertEquals(1, results.size());
	}

	protected static class Outer {
		public static final String SOME_STRING_FIELD_NAME = "someString";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = SOME_STRING_FIELD_NAME, foreign = true)
		Foo foreign;
		public Outer() {
		}
	}

	protected static class Inner {
		public static final String SOME_STRING_FIELD_NAME = "someString";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = SOME_STRING_FIELD_NAME, foreign = true)
		Foo foreign;
		public Inner() {
		}
	}
}
