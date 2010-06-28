package com.j256.ormlite.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.db.H2DatabaseType;

public class DatabaseTypeFactoryTest {

	@Test
	public void create() throws Exception {
		DatabaseTypeFactory databaseTypeFactory = new DatabaseTypeFactory();
		String url = "jdbc:h2:mem:test";
		databaseTypeFactory.setDatabaseUrl(url);
		databaseTypeFactory.initialize();

		assertEquals(url, databaseTypeFactory.getDatabaseUrl());
		assertEquals(new H2DatabaseType().getDriverClassName(), databaseTypeFactory.getDriverClassName());
		assertTrue(databaseTypeFactory.getDatabaseType().getClass() == H2DatabaseType.class);
	}

	@Test(expected = IllegalStateException.class)
	public void badSpringWiring() throws Exception {
		new DatabaseTypeFactory().initialize();
	}
}
