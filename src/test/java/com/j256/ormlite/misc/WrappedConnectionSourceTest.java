package com.j256.ormlite.misc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.h2.H2ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class WrappedConnectionSourceTest {

	@Test
	public void testStuff() throws Exception {
		WrappedConnectionSource cs = new WrappedConnectionSource(new H2ConnectionSource());
		cs.close();
	}

	@Test
	public void testNotClosedInterator() throws Exception {
		WrappedConnectionSource cs = new WrappedConnectionSource(new H2ConnectionSource());
		Dao<WrappedFoo, ?> dao = DaoManager.createDao(cs, WrappedFoo.class);
		TableUtils.createTable(dao);
		WrappedFoo foo1 = new WrappedFoo();
		assertEquals(1, dao.create(foo1));
		WrappedFoo foo2 = new WrappedFoo();
		assertEquals(1, dao.create(foo2));
		CloseableIterator<WrappedFoo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		WrappedFoo result = iterator.next();
		assertNotNull(result);
		assertEquals(foo1.id, result.id);
		assertThrowsExactly(SQLException.class, () -> {
			cs.close();
		});
	}

	@Test
	public void testCloseBad() throws Exception {
		@SuppressWarnings("resource")
		WrappedConnectionSource cs = new WrappedConnectionSource(new H2ConnectionSource());
		assertThrowsExactly(SQLException.class, () -> {
			cs.releaseConnection(null);
			cs.close();
		});
	}

	private static class WrappedFoo {

		@DatabaseField(generatedId = true)
		private int id;

		public WrappedFoo() {
			// for ormlite
		}
	}
}
