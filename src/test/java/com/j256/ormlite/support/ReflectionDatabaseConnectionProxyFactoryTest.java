package com.j256.ormlite.support;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.h2.H2ConnectionSource;

public class ReflectionDatabaseConnectionProxyFactoryTest extends BaseCoreTest {

	private static final int VALUE_INCREMENT = 13;

	@BeforeClass
	public static void beforeClass() {
		ReflectionDatabaseConnectionProxyFactory factory =
				new ReflectionDatabaseConnectionProxyFactory(OurConnectionProxy.class);
		H2ConnectionSource.setDatabaseConnectionProxyFactory(factory);
	}

	@AfterClass
	public static void afterClass() {
		H2ConnectionSource.setDatabaseConnectionProxyFactory(null);
	}

	@Override
	@Before
	public void before() throws Exception {
		super.before();
		OurConnectionProxy.insertCount = 0;
	}

	@Test
	public void testBasic() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.val = 1131233;

		assertEquals(0, OurConnectionProxy.insertCount);
		assertEquals(1, dao.create(foo));
		assertEquals(1, OurConnectionProxy.insertCount);

		Foo result = dao.queryForId(foo.id);
		assertEquals(foo.val + VALUE_INCREMENT, result.val);
	}

	private static class OurConnectionProxy extends DatabaseConnectionProxy {

		static int insertCount;

		public OurConnectionProxy(DatabaseConnection proxy) {
			super(proxy);
		}

		@Override
		public int insert(String statement, Object[] args, FieldType[] argfieldTypes, GeneratedKeyHolder keyHolder)
				throws SQLException {
			// change the first argument which should be the 'val' field
			int valIdx = 0;
			for (int i = 0; i < argfieldTypes.length; ++i) {
				if (argfieldTypes[i].getColumnName().equals("val")) {
					valIdx = i;
					break;
				}
			}
			args[valIdx] = (Integer) args[valIdx] + VALUE_INCREMENT;
			insertCount++;
			return super.insert(statement, args, argfieldTypes, keyHolder);
		}
	}
}
