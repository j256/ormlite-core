package com.j256.ormlite.support;

import org.junit.Test;

import com.j256.ormlite.dao.BaseDaoImpl;

public class SimpleJdbcDaoSupportTest {

	@Test(expected = IllegalStateException.class)
	public void testSpringBadWiring() throws Exception {
		BaseDaoImpl<String, String> daoSupport = new BaseDaoImpl<String, String>(String.class) {
		};
		daoSupport.initialize();
	}
}
