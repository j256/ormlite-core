package com.j256.ormlite.support;

import org.junit.Test;

public class SimpleJdbcDaoSupportTest {

	@Test(expected = IllegalStateException.class)
	public void testSpringBadWiring() throws Exception {
		SimpleJdbcDaoSupport daoSupport = new SimpleJdbcDaoSupport() {
		};
		daoSupport.initialize();
	}
}
