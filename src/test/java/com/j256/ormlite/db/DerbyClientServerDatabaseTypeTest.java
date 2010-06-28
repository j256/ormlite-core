package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DerbyClientServerDatabaseTypeTest extends DerbyEmbeddedDatabaseTypeTest {

	@Test
	public void testGetClientServerDriverClassName() {
		assertEquals("org.apache.derby.jdbc.ClientDriver", new DerbyClientServerDatabaseType().getDriverClassName());
	}
}
