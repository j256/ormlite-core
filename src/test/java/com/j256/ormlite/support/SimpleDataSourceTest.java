package com.j256.ormlite.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Test;

public class SimpleDataSourceTest {

	@Test
	public void testSimpleDataSource() {
		SimpleDataSource sds = new SimpleDataSource();
		int timeout = 123213;
		sds.setLoginTimeout(timeout);
		assertEquals(timeout, sds.getLoginTimeout());
		String url = "foo:bar:baz";
		sds.setUrl(url);
		assertEquals(url, sds.getUrl());
		PrintWriter logWriter = new PrintWriter(System.err);
		sds.setLogWriter(logWriter);
		assertEquals(logWriter, sds.getLogWriter());
	}

	@Test
	public void testSimpleDataSourceString() {
		String url = "foo:bar:baz";
		SimpleDataSource sds = new SimpleDataSource(url);
		assertEquals(url, sds.getUrl());
	}

	@Test
	public void testSimpleDataSourceStringStringString() throws Exception {
		String url = "foo:bar:baz";
		String username = "user";
		String password = "_secret";
		SimpleDataSource sds = new SimpleDataSource(url, username, password);
		Connection conn = createMock(Connection.class);
		Driver driver = createMock(Driver.class);
		Properties props = new Properties();
		props.put("user", username);
		props.put("password", password);
		expect(driver.connect(eq(url), eq(props))).andReturn(conn);
		replay(driver);
		DriverManager.registerDriver(driver);
		try {
			assertNotNull(sds.getConnection());
			verify(driver);
		} finally {
			DriverManager.deregisterDriver(driver);
		}
	}

	@Test
	public void testGetConnection() throws Exception {
		String url = "foo:bar:baz";
		SimpleDataSource sds = new SimpleDataSource(url);
		Connection conn = createMock(Connection.class);
		Driver driver = createMock(Driver.class);
		expect(driver.connect(isA(String.class), isA(Properties.class))).andReturn(conn);
		replay(driver);
		DriverManager.registerDriver(driver);
		try {
			assertNotNull(sds.getConnection());
			verify(driver);
		} finally {
			DriverManager.deregisterDriver(driver);
		}
	}

	@Test
	public void testGetConnectionStringString() throws Exception {
		String url = "foo:bar:baz";
		String username = "user";
		String password = "_secret";
		SimpleDataSource sds = new SimpleDataSource(url);
		Connection conn = createMock(Connection.class);
		Driver driver = createMock(Driver.class);
		Properties props = new Properties();
		props.put("user", username);
		props.put("password", password);
		expect(driver.connect(eq(url), eq(props))).andReturn(conn);
		replay(driver);
		DriverManager.registerDriver(driver);
		try {
			assertNotNull(sds.getConnection(username, password));
			verify(driver);
		} finally {
			DriverManager.deregisterDriver(driver);
		}
	}

	@Test
	public void testGetConnectionUserPassSetters() throws Exception {
		String url = "foo:bar:baz";
		String username = "user";
		String password = "_secret";
		SimpleDataSource sds = new SimpleDataSource(url);
		sds.setUsername(username);
		sds.setPassword(password);
		Connection conn = createMock(Connection.class);
		Driver driver = createMock(Driver.class);
		Properties props = new Properties();
		props.put("user", username);
		props.put("password", password);
		expect(driver.connect(eq(url), eq(props))).andReturn(conn);
		replay(driver);
		DriverManager.registerDriver(driver);
		try {
			assertNotNull(sds.getConnection());
			verify(driver);
		} finally {
			DriverManager.deregisterDriver(driver);
		}
	}

	@Test(expected = SQLException.class)
	public void testGetConnectionNull() throws Exception {
		String url = "foo:bar:baz";
		SimpleDataSource sds = new SimpleDataSource(url);
		Driver driver = createMock(Driver.class);
		Properties props = new Properties();
		expect(driver.connect(eq(url), eq(props))).andReturn(null);
		replay(driver);
		DriverManager.registerDriver(driver);
		try {
			sds.getConnection();
		} finally {
			DriverManager.deregisterDriver(driver);
		}
	}

	@Test
	public void testDestroy() throws Exception {
		String url = "foo:bar:baz";
		SimpleDataSource sds = new SimpleDataSource(url);
		Connection conn = createMock(Connection.class);
		conn.close();
		Driver driver = createMock(Driver.class);
		expect(driver.connect(isA(String.class), isA(Properties.class))).andReturn(conn);
		replay(driver);
		replay(conn);
		DriverManager.registerDriver(driver);
		try {
			assertNotNull(sds.getConnection());
			sds.destroy();
			verify(driver);
			verify(conn);
		} finally {
			DriverManager.deregisterDriver(driver);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testInitNoUrl() {
		new SimpleDataSource().initialize();
	}

	@Test(expected = SQLException.class)
	public void testConnectionClosed() throws Exception {
		String url = "foo:bar:baz";
		SimpleDataSource sds = new SimpleDataSource(url);
		Connection conn = createMock(Connection.class);
		expect(conn.isClosed()).andReturn(true);
		Driver driver = createMock(Driver.class);
		expect(driver.connect(isA(String.class), isA(Properties.class))).andReturn(conn);
		replay(driver);
		replay(conn);
		DriverManager.registerDriver(driver);
		try {
			assertNotNull(sds.getConnection());
			sds.getConnection();
			fail("Should not get here");
		} finally {
			DriverManager.deregisterDriver(driver);
		}
	}

	@Test
	public void testJava6Methods() throws Exception {
		String url = "foo:bar:baz";
		SimpleDataSource sds = new SimpleDataSource(url);
		assertFalse(sds.isWrapperFor(Driver.class));
		assertNull(sds.unwrap(Driver.class));
	}

	@Test
	public void testSpringWiring() throws Exception {
		String url = "foo:bar:baz";
		SimpleDataSource sds = new SimpleDataSource();
		sds.setUrl(url);
		sds.initialize();
	}
}
