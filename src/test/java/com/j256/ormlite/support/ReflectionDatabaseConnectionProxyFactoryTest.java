package com.j256.ormlite.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.h2.H2ConnectionSource;

public class ReflectionDatabaseConnectionProxyFactoryTest extends BaseCoreTest {

	private static final int VALUE_INCREMENT = 13;

	@BeforeAll
	public static void beforeClass() {
		ReflectionDatabaseConnectionProxyFactory factory =
				new ReflectionDatabaseConnectionProxyFactory(OurConnectionProxy.class);
		H2ConnectionSource.setDatabaseConnectionProxyFactory(factory);
	}

	@AfterAll
	public static void afterClass() {
		H2ConnectionSource.setDatabaseConnectionProxyFactory(null);
	}

	@Override
	@BeforeEach
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
			// change the 'val' field argument
			for (int i = 0; i < argfieldTypes.length; ++i) {
				if (argfieldTypes[i].getColumnName().equals(Foo.VAL_COLUMN_NAME)) {
					args[i] = (Integer) args[i] + VALUE_INCREMENT;
					break;
				}
			}
			insertCount++;
			return super.insert(statement, args, argfieldTypes, keyHolder);
		}
	}
}
