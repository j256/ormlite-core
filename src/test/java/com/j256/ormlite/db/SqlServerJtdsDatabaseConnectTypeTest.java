package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

public class SqlServerJtdsDatabaseConnectTypeTest extends SqlServerDatabaseTypeTest {

	@Override
	protected void setDatabaseParams() throws SQLException {
		databaseUrl = "jdbc:jtds:sqlserver://db/ormlite;ssl=request";
		connectionSource = DatabaseTypeUtils.createJdbcConnectionSource(DEFAULT_DATABASE_URL);
	}

	@Override
	@Test
	public void testGetDriverClassName() {
		assertEquals("net.sourceforge.jtds.jdbc.Driver", databaseType.getDriverClassName());
	}
}
