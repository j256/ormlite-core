package com.j256.ormlite.dao;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;

public class DaoManagerTest extends BaseCoreTest {

	@Test
	public void testCreateClass() throws Exception {
		testClass(Foo.class);
		DatabaseTableConfig<Foo> tableConfig =
				new DatabaseTableConfig<Foo>(Foo.class, Arrays.asList(new DatabaseFieldConfig("id", null,
						DataType.UNKNOWN, null, 0, false, false, false, null, false, null, false, null, false, null,
						false, null, null, false, DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED, 0)));
		testTable(tableConfig);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateDaoNull() throws Exception {
		DaoManager.createDao(null, Foo.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLookupDaoNull() {
		DaoManager.lookupDao(null, Foo.class);
	}

	@Test
	public void testLookupDaoUnknown() {
		assertNull(DaoManager.lookupDao(connectionSource, getClass()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateDaoTableNull() throws Exception {
		DaoManager.createDao(null, new DatabaseTableConfig<Foo>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLookupDaoTableNull() {
		DaoManager.lookupDao(null, new DatabaseTableConfig<Foo>());
	}

	@Test
	public void testRegisterDao() throws Exception {
		Dao<RegisterClass, Void> dao = DaoManager.lookupDao(connectionSource, RegisterClass.class);
		assertNull(dao);
		Dao<? extends RegisterClass, Object> daoImpl = BaseDaoImpl.createDao(connectionSource, RegisterClass.class);
		DaoManager.registerDao(connectionSource, daoImpl);
		dao = DaoManager.lookupDao(connectionSource, RegisterClass.class);
		assertSame(daoImpl, dao);
	}

	@Test
	public void testRegisterDaoTable() throws Exception {
		DatabaseTableConfig<Bar> tableConfig =
				new DatabaseTableConfig<Bar>(Bar.class, Arrays.asList(new DatabaseFieldConfig("foo", null,
						DataType.UNKNOWN, null, 0, false, false, false, null, false, null, false, null, false, null,
						false, null, null, false, DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED, 0)));
		Dao<Bar, Void> dao = DaoManager.lookupDao(connectionSource, tableConfig);
		assertNull(dao);
		Dao<? extends Bar, Object> daoImpl = BaseDaoImpl.createDao(connectionSource, tableConfig);
		DaoManager.registerDaoWithTableConfig(connectionSource, daoImpl);
		dao = DaoManager.lookupDao(connectionSource, tableConfig);
		assertSame(daoImpl, dao);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRegisterNull() {
		DaoManager.registerDao(null, null);
	}

	@Test
	public void testLookupTableDaoUnknown() {
		assertNull(DaoManager.lookupDao(connectionSource, new DatabaseTableConfig<DaoManagerTest>(DaoManagerTest.class,
				new ArrayList<DatabaseFieldConfig>())));
	}

	@Test
	public void testDaoClassBaseDaoImpl() throws Exception {
		testClass(Bar.class);
		DatabaseTableConfig<Bar> tableConfig =
				new DatabaseTableConfig<Bar>(Bar.class, Arrays.asList(new DatabaseFieldConfig("foo", null,
						DataType.UNKNOWN, null, 0, false, false, false, null, false, null, false, null, false, null,
						false, null, null, false, DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED, 0)));
		testTable(tableConfig);
	}

	@Test
	public void testDaoClassDifferentDao() throws Exception {
		testClass(Baz.class);
		DatabaseTableConfig<Baz> tableConfig =
				new DatabaseTableConfig<Baz>(Baz.class, Arrays.asList(new DatabaseFieldConfig("foo", null,
						DataType.UNKNOWN, null, 0, false, false, false, null, false, null, false, null, false, null,
						false, null, null, false, DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED, 0)));
		testTable(tableConfig);
	}

	@Test
	public void testPrivateConstructorDao() throws Exception {
		try {
			testClass(PrivateConstructor.class);
			fail("exception expected");
		} catch (SQLException e) {
			// expected
		}
		DatabaseTableConfig<PrivateConstructor> tableConfig =
				new DatabaseTableConfig<PrivateConstructor>(PrivateConstructor.class,
						Arrays.asList(new DatabaseFieldConfig("foo", null, DataType.UNKNOWN, null, 0, false, false,
								false, null, false, null, false, null, false, null, false, null, null, false,
								DatabaseField.DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL, 0)));
		try {
			testTable(tableConfig);
			fail("exception expected");
		} catch (SQLException e) {
			// expected
		}
	}

	@Test
	public void testCouldNotConstruct() throws Exception {
		try {
			testClass(ConstructorFail.class);
			fail("exception expected");
		} catch (SQLException e) {
			// expected
		}
		DatabaseTableConfig<ConstructorFail> tableConfig =
				new DatabaseTableConfig<ConstructorFail>(ConstructorFail.class, Arrays.asList(new DatabaseFieldConfig(
						"foo", null, DataType.UNKNOWN, null, 0, false, false, false, null, false, null, false, null,
						false, null, false, null, null, false, 0, 0)));
		try {
			testTable(tableConfig);
			fail("exception expected");
		} catch (SQLException e) {
			// expected
		}
	}

	@Test
	public void testDaoClassGenericDao() throws Exception {
		testClass(GenericBar.class);
		DatabaseTableConfig<GenericBar> tableConfig =
				new DatabaseTableConfig<GenericBar>(GenericBar.class, Arrays.asList(new DatabaseFieldConfig("foo",
						null, DataType.UNKNOWN, null, 0, false, false, false, null, false, null, false, null, false,
						null, false, null, null, false,
						DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED, 0)));
		testTable(tableConfig);
	}

	@Test
	public void testDaoClassGenericDaoWithId() throws Exception {
		testClass(GenericBaz.class);
		DatabaseTableConfig<GenericBaz> tableConfig =
				new DatabaseTableConfig<GenericBaz>(GenericBaz.class, Arrays.asList(new DatabaseFieldConfig("foo",
						null, DataType.UNKNOWN, null, 0, false, false, false, null, false, null, false, null, false,
						null, false, null, null, false,
						DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED, 0)));
		testTable(tableConfig);
	}

	@Test
	public void testDaoClassGenericDaoMethod() throws Exception {
		GenericDao<GenericBaz, String> bazdao = DaoManager.createDao(connectionSource, GenericBaz.class);
		assertSame(GenericBaz.class.getName(), bazdao.doGenericAction());
		GenericDao<GenericBar, Void> bardao = DaoManager.createDao(connectionSource, GenericBar.class);
		assertSame(GenericBar.class.getName(), bardao.doGenericAction());
	}

	@Test
	public void testSelfReferenceWithLoadedConfig() throws Exception {
		DaoManager.clearCache();
		/*
		 * If a class was loaded as a config (this was found under Android) then, when it went recursive it would build
		 * itself and set its foreign field to be a primitive. Then when it re-configured itself it would scream because
		 * the primitive was marked as foreign.
		 * 
		 * The answer was to do a better job of pre-caching the DAOs in the DaoManager.
		 */
		DatabaseTableConfig<SelfReference> config =
				DatabaseTableConfig.fromClass(connectionSource, SelfReference.class);
		@SuppressWarnings("unchecked")
		List<DatabaseTableConfig<?>> configs = new ArrayList<DatabaseTableConfig<?>>(Arrays.asList(config));
		DaoManager.addCachedDatabaseConfigs(configs);
		// this used to throw an exception
		DaoManager.createDao(connectionSource, SelfReference.class);
	}

	@Test
	public void testClassLoopWithLoadedConfig() throws Exception {
		DaoManager.clearCache();
		DatabaseTableConfig<LoopOne> config1 = DatabaseTableConfig.fromClass(connectionSource, LoopOne.class);
		DatabaseTableConfig<LoopTwo> config2 = DatabaseTableConfig.fromClass(connectionSource, LoopTwo.class);
		@SuppressWarnings("unchecked")
		List<DatabaseTableConfig<?>> configs = new ArrayList<DatabaseTableConfig<?>>(Arrays.asList(config1, config2));
		DaoManager.addCachedDatabaseConfigs(configs);
		assertNotNull(DaoManager.createDao(connectionSource, LoopOne.class));
		assertNotNull(DaoManager.createDao(connectionSource, LoopTwo.class));
	}

	@Test
	public void testMoreComplexClassLoopWithLoadedConfig() throws Exception {
		DaoManager.clearCache();
		DatabaseTableConfig<MoreComplexLoopOne> config1 =
				DatabaseTableConfig.fromClass(connectionSource, MoreComplexLoopOne.class);
		DatabaseTableConfig<MoreComplexLoopTwo> config2 =
				DatabaseTableConfig.fromClass(connectionSource, MoreComplexLoopTwo.class);
		DatabaseTableConfig<MoreComplexLoopThree> config3 =
				DatabaseTableConfig.fromClass(connectionSource, MoreComplexLoopThree.class);
		DatabaseTableConfig<MoreComplexLoopFour> config4 =
				DatabaseTableConfig.fromClass(connectionSource, MoreComplexLoopFour.class);
		@SuppressWarnings("unchecked")
		List<DatabaseTableConfig<?>> configs =
				new ArrayList<DatabaseTableConfig<?>>(Arrays.asList(config1, config2, config3, config4));
		DaoManager.addCachedDatabaseConfigs(configs);
		assertNotNull(DaoManager.createDao(connectionSource, MoreComplexLoopOne.class));
		assertNotNull(DaoManager.createDao(connectionSource, MoreComplexLoopTwo.class));
		assertNotNull(DaoManager.createDao(connectionSource, MoreComplexLoopThree.class));
		assertNotNull(DaoManager.createDao(connectionSource, MoreComplexLoopFour.class));
	}

	/* ================================================================== */

	private <T> void testClass(Class<T> clazz) throws Exception {
		Dao<T, Void> dao1 = DaoManager.createDao(connectionSource, clazz);
		Dao<T, Void> dao2 = DaoManager.createDao(connectionSource, clazz);
		Dao<T, ?> dao3 = DaoManager.lookupDao(connectionSource, clazz);
		Dao<Foreign, Void> dao4 = DaoManager.createDao(connectionSource, Foreign.class);
		ConnectionSource otherConnectionSource = createMock(ConnectionSource.class);
		expect(otherConnectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		replay(otherConnectionSource);
		Dao<T, Void> dao5 = DaoManager.createDao(otherConnectionSource, clazz);
		assertSame(dao1, dao2);
		assertSame(dao1, dao3);
		assertNotSame(dao1, dao4);
		assertNotSame(dao1, dao5);
		DaoManager.clearCache();
		dao2 = DaoManager.createDao(connectionSource, clazz);
		assertNotSame(dao1, dao2);
	}

	private <T> void testTable(DatabaseTableConfig<T> config) throws Exception {
		Dao<T, Void> dao1 = DaoManager.createDao(connectionSource, config);
		Dao<T, Void> dao2 = DaoManager.createDao(connectionSource, config);
		Dao<T, Void> dao3 = DaoManager.lookupDao(connectionSource, config);
		assertSame(dao1, dao2);
		assertSame(dao1, dao3);
		DaoManager.clearCache();
		Dao<T, ?> dao4 = DaoManager.createDao(connectionSource, config);
		assertNotSame(dao4, dao2);
	}

	/* ================================================================== */

	@DatabaseTable(daoClass = BaseDaoImpl.class)
	protected static class Bar {
		@DatabaseField
		String foo;
		public Bar() {
		}
	}

	@DatabaseTable(daoClass = BazDao.class)
	protected static class Baz {
		@DatabaseField
		String foo;
		public Baz() {
		}
	}

	@DatabaseTable(daoClass = PrivateConstructorDao.class)
	protected static class PrivateConstructor {
		@DatabaseField
		String foo;
		public PrivateConstructor() {
		}
	}

	@DatabaseTable(daoClass = ConstructorFailDao.class)
	protected static class ConstructorFail {
		@DatabaseField
		String foo;
		public ConstructorFail() {
		}
	}

	@DatabaseTable(daoClass = GenericDao.class)
	protected static class GenericBar extends Bar {
		@DatabaseField
		String foo;
		public GenericBar() {
		}
	}

	@DatabaseTable(daoClass = GenericDao.class)
	protected static class GenericBaz extends Baz {
		@DatabaseField(id = true)
		String fooId;
		@DatabaseField
		String foo;
		public GenericBaz() {
		}
	}

	protected static class RegisterClass {
		@DatabaseField
		String foo;
		public RegisterClass() {
		}
	}

	public static class SelfReference {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		SelfReference foreign;
		@ForeignCollectionField
		ForeignCollection<SelfReference> others;
		public SelfReference() {
		}
	}

	public static class LoopOne {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		LoopTwo foreign;
		@ForeignCollectionField
		ForeignCollection<LoopTwo> twos;
		public LoopOne() {
		}
	}

	public static class LoopTwo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		LoopOne foreign;
		@ForeignCollectionField
		ForeignCollection<LoopOne> ones;
		public LoopTwo() {
		}
	}

	public static class MoreComplexLoopOne {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		MoreComplexLoopOne foreign;
		@DatabaseField(foreign = true)
		MoreComplexLoopFour four;
		@ForeignCollectionField
		ForeignCollection<MoreComplexLoopTwo> two;
		public MoreComplexLoopOne() {
		}
	}

	public static class MoreComplexLoopTwo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		MoreComplexLoopOne foreign;
		@ForeignCollectionField
		ForeignCollection<MoreComplexLoopThree> threes;
		public MoreComplexLoopTwo() {
		}
	}

	public static class MoreComplexLoopThree {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		MoreComplexLoopTwo foreign;
		@DatabaseField(foreign = true)
		MoreComplexLoopThree self;
		@ForeignCollectionField
		ForeignCollection<MoreComplexLoopFour> fours;
		public MoreComplexLoopThree() {
		}
	}

	public static class MoreComplexLoopFour {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		MoreComplexLoopThree foreign;
		@ForeignCollectionField
		ForeignCollection<MoreComplexLoopOne> ones;
		public MoreComplexLoopFour() {
		}
	}

	/* ================================================================== */

	public static class BazDao extends BaseDaoImpl<Baz, Void> {
		public BazDao(ConnectionSource connectionSource) throws SQLException {
			super(connectionSource, Baz.class);
		}
		public BazDao(ConnectionSource connectionSource, DatabaseTableConfig<Baz> tableConfig) throws SQLException {
			super(connectionSource, tableConfig);
		}
	}

	public static class PrivateConstructorDao extends BaseDaoImpl<PrivateConstructor, Void> {
		private PrivateConstructorDao(ConnectionSource connectionSource) throws SQLException {
			super(connectionSource, PrivateConstructor.class);
		}
		private PrivateConstructorDao(ConnectionSource connectionSource,
				DatabaseTableConfig<PrivateConstructor> tableConfig) throws SQLException {
			super(connectionSource, tableConfig);
		}
	}

	public static class ConstructorFailDao extends BaseDaoImpl<ConstructorFail, Void> {
		public ConstructorFailDao(ConnectionSource connectionSource) throws SQLException {
			super(connectionSource, ConstructorFail.class);
			throw new RuntimeException("throw throw throw");
		}
		public ConstructorFailDao(ConnectionSource connectionSource, DatabaseTableConfig<ConstructorFail> tableConfig)
				throws SQLException {
			super(connectionSource, tableConfig);
			throw new RuntimeException("throw throw throw");
		}
	}

	public static class GenericDao<T, ID> extends BaseDaoImpl<T, ID> {
		public GenericDao(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
			super(connectionSource, dataClass);
		}
		public GenericDao(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig) throws SQLException {
			super(connectionSource, tableConfig);
		}
		public String doGenericAction() {
			return getDataClass().getName();
		}
	}
}
