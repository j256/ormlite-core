package com.j256.ormlite.support;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.h2.H2ConnectionSource;

public class DatabaseConnectionProxyFactoryTest extends BaseCoreTest {

	@BeforeClass
	public static void beforeClass() {
		H2ConnectionSource.setDatabaseConnectionProxyFactory(new DatabaseConnectionProxyFactory() {
			public DatabaseConnection createProxy(DatabaseConnection realConnection) {
				return new ConnectionProxy(realConnection);
			}
		});
	}

	@AfterClass
	public static void afterClass() {
		H2ConnectionSource.setDatabaseConnectionProxyFactory(null);
	}

	@Test
	public void testBasic() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.val = 100;

		assertEquals(0, ConnectionProxy.lastValue);
		assertEquals(1, dao.create(foo));
		assertEquals(foo.val, ConnectionProxy.lastValue);
	}

	private static class ConnectionProxy extends DatabaseConnectionProxy {
		static int lastValue;
		public ConnectionProxy(DatabaseConnection conn) {
			super(conn);
		}
		@Override
		public int insert(String statement, Object[] args, FieldType[] argfieldTypes, GeneratedKeyHolder keyHolder)
				throws SQLException {
			// just record the first argument to the insert
			lastValue = (Integer) args[0];
			return super.insert(statement, args, argfieldTypes, keyHolder);
		}
	}
}
