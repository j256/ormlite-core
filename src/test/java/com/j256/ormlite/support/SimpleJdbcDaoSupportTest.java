package com.j256.ormlite.support;

import org.junit.Test;

public class SimpleJdbcDaoSupportTest {

	@Test(expected = IllegalStateException.class)
	public void testSpringBadWiring() throws Exception {
		SimpleDaoSupport daoSupport = new SimpleDaoSupport() {
		};
		daoSupport.initialize();
	}
}
