package com.j256.ormlite.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.dao.Dao;

public class DeleteBuilderTest extends BaseCoreStmtTest {

	@Test
	public void testDeleteAll() throws Exception {
		DeleteBuilder<Foo, Integer> stmtb = new DeleteBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testDeleteMethod() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.val = 123123;
		assertEquals(1, dao.create(foo));

		assertNotNull(dao.queryForId(foo.id));
		DeleteBuilder<Foo, Integer> db = dao.deleteBuilder();
		// no match
		db.where().eq(Foo.VAL_COLUMN_NAME, foo.val + 1);
		assertEquals(0, db.delete());
		assertNotNull(dao.queryForId(foo.id));

		db.where().reset();
		db.where().eq(Foo.VAL_COLUMN_NAME, foo.val);
		assertEquals(1, db.delete());
		assertNull(dao.queryForId(foo.id));
	}

	@Test
	public void testUpdateLimit() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		int num = 3;
		for (int i = 0; i < num; i++) {
			dao.create(new Foo());
		}
		long limit = 2;
		assertEquals(limit, dao.deleteBuilder().limit(limit).delete());
		int count = (int) dao.countOf();
		assertEquals(num - limit, count);
		assertNotEquals(num, count);
	}
}
