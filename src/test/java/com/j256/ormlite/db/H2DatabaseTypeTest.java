package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.TestUtils;
import com.j256.ormlite.table.TableInfo;

public class H2DatabaseTypeTest extends BaseDatabaseTest {

	private final static String DATABASE_NAME = "ormlite";
	private final String DB_DIRECTORY = "target/" + getClass().getSimpleName();

	@Override
	protected void setDatabaseParams() {
		databaseUrl = "jdbc:h2:mem:" + DATABASE_NAME;
	}

	@Test
	public void testGetDriverClassName() {
		assertEquals("org.h2.Driver", databaseType.getDriverClassName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGeneratedIdSequenceNotSupported() throws Exception {
		TableInfo<GeneratedIdSequence> tableInfo =
				new TableInfo<GeneratedIdSequence>(databaseType, GeneratedIdSequence.class);
		assertEquals(2, tableInfo.getFieldTypes().length);
		StringBuilder sb = new StringBuilder();
		ArrayList<String> additionalArgs = new ArrayList<String>();
		ArrayList<String> statementsBefore = new ArrayList<String>();
		databaseType.appendColumnArg(sb, tableInfo.getFieldTypes()[0], additionalArgs, statementsBefore, null, null);
	}

	@Test
	public void testUsernamePassword() throws Exception {
		closeConnection();
		databaseType = new DerbyEmbeddedDatabaseType();
	}

	@Test(expected = SQLException.class)
	public void testRemotePort() throws Exception {
		File dbDir = new File(DB_DIRECTORY);
		TestUtils.deleteDirectory(dbDir);
		dbDir.mkdirs();
		// bad port
		int notTheRightPort = 12345;
		closeConnection();
		// try to disable the retry feature which delays this test failure
		System.setProperty("h2.socketConnectRetry", "0");
		String dbUrl = "jdbc:h2:tcp://localhost:" + notTheRightPort + "/" + dbDir.getPath() + "/" + DATABASE_NAME;
		connectionSource = DatabaseTypeUtils.createJdbcConnectionSource(dbUrl);
		connectionSource.getReadOnlyConnection();
		DatabaseTypeUtils.createDatabaseType(dbUrl);
	}
}
