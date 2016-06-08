package com.j256.ormlite.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.stmt.StatementBuilder.StatementType;

public class DatabaseConnectionProxyTest {

	@Test
	public void testIsAutoCommitSupported() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		boolean supported = true;
		expect(conn.isAutoCommitSupported()).andReturn(supported);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(supported, proxy.isAutoCommitSupported());
		proxy.close();
		verify(conn);
	}

	@Test
	public void testIsAutoCommitSupportedNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertFalse(proxy.isAutoCommitSupported());
		proxy.close();
	}

	@Test
	public void testIsAutoCommit() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		boolean autoCommit = false;
		expect(conn.isAutoCommit()).andReturn(autoCommit);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(autoCommit, proxy.isAutoCommit());
		proxy.close();
		verify(conn);
	}

	@Test
	public void testIsAutoCommitNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertFalse(proxy.isAutoCommit());
		proxy.close();
	}

	@Test
	public void testSetAutoCommit() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		boolean autoCommit = false;
		conn.setAutoCommit(autoCommit);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.setAutoCommit(autoCommit);
		proxy.close();
		verify(conn);
	}

	@Test
	public void testSetAutoCommitNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		proxy.setAutoCommit(false);
		proxy.close();
	}

	@Test
	public void testSetSavePoint() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String name = "savepoint";
		expect(conn.setSavePoint(name)).andReturn(null);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.setSavePoint(name);
		proxy.close();
		verify(conn);
	}

	@Test
	public void testSetSavePointNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertNull(proxy.setSavePoint("name"));
		proxy.close();
	}

	@Test
	public void testCommit() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		conn.commit(null);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.commit(null);
		proxy.close();
		verify(conn);
	}

	@Test
	public void testCommitNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		proxy.commit(null);
		proxy.close();
	}

	@Test
	public void testRollback() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		conn.rollback(null);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.rollback(null);
		proxy.close();
		verify(conn);
	}

	@Test
	public void testRollbackNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		proxy.rollback(null);
		proxy.close();
	}

	@Test
	public void testExecuteStatement() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "select foo from bar";
		int result = 1312321;
		expect(conn.executeStatement(statement, 0)).andReturn(result);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.executeStatement(statement, 0));
		proxy.close();
		verify(conn);
	}

	@Test
	public void testExecuteStatementNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertEquals(0, proxy.executeStatement("statment", 0));
		proxy.close();
	}

	@Test
	public void testCompileStatementStringStatementTypeFieldTypeArrayInt() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "select foo from bar";
		StatementType type = StatementType.DELETE;
		int flags = 11253123;
		expect(conn.compileStatement(statement, type, null, flags, false)).andReturn(null);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.compileStatement(statement, type, null, flags, false);
		proxy.close();
		verify(conn);
	}

	@Test
	public void testCompileStatementStringStatementTypeFieldTypeArrayIntNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertNull(proxy.compileStatement("statment", StatementType.DELETE, null, 0, false));
		proxy.close();
	}

	@Test
	public void testInsert() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "insert bar";
		int result = 13712321;
		expect(conn.insert(statement, null, null, null)).andReturn(result);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.insert(statement, null, null, null));
		proxy.close();
		verify(conn);
	}

	@Test
	public void testInsertNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertEquals(0, proxy.insert("statment", null, null, null));
		proxy.close();
	}

	@Test
	public void testUpdate() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "insert bar";
		int result = 13212321;
		expect(conn.update(statement, null, null)).andReturn(result);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.update(statement, null, null));
		proxy.close();
		verify(conn);
	}

	@Test
	public void testUpdateNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertEquals(0, proxy.update("statment", null, null));
		proxy.close();
	}

	@Test
	public void testDelete() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "insert bar";
		int result = 13872321;
		expect(conn.delete(statement, null, null)).andReturn(result);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.delete(statement, null, null));
		proxy.close();
		verify(conn);
	}

	@Test
	public void testDeleteNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertEquals(0, proxy.delete("statment", null, null));
		proxy.close();
	}

	@Test
	public void testQueryForOne() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "insert bar";
		Object result = new Object();
		expect(conn.queryForOne(statement, null, null, null, null)).andReturn(result);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.queryForOne(statement, null, null, null, null));
		proxy.close();
		verify(conn);
	}

	@Test
	public void testQueryForOneNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertNull(proxy.queryForOne("statment", null, null, null, null));
		proxy.close();
	}

	@Test
	public void testQueryForLongString() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "select stuff from foo";
		long result = 31231231241414L;
		expect(conn.queryForLong(statement)).andReturn(result);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.queryForLong(statement));
		proxy.close();
		verify(conn);
	}

	@Test
	public void testQueryForLongStringNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertEquals(0, proxy.queryForLong("statment"));
		proxy.close();
	}

	@Test
	public void testQueryForLongStringObjectArrayFieldTypeArray() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "select stuff from foo";
		long result = 3123123124141413L;
		expect(conn.queryForLong(statement, null, null)).andReturn(result);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.queryForLong(statement, null, null));
		proxy.close();
		verify(conn);
	}

	@Test
	public void testQueryForLongStringObjectArrayFieldTypeArrayNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertEquals(0, proxy.queryForLong("statment", null, null));
		proxy.close();
	}

	@Test
	public void testClose() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.close();
		verify(conn);
	}

	@Test
	public void testCloseNull() throws Exception {
		new DatabaseConnectionProxy(null).close();
	}

	@Test
	public void testCloseQuietly() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		conn.closeQuietly();
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.closeQuietly();
		proxy.close();
		verify(conn);
	}

	@Test
	public void testCloseQuietlyNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		proxy.closeQuietly();
		proxy.close();
	}

	@Test
	public void testIsClosed() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		boolean closed = true;
		expect(conn.isClosed()).andReturn(closed);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(closed, proxy.isClosed());
		proxy.close();
		verify(conn);
	}

	@Test
	public void testIsClosedNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertTrue(proxy.isClosed());
		proxy.close();
	}

	@Test
	public void testIsTableExists() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		boolean tableExists = true;
		String tableName = "fjewfjwef";
		expect(conn.isTableExists(tableName)).andReturn(tableExists);
		conn.close();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(tableExists, proxy.isTableExists(tableName));
		proxy.close();
		verify(conn);
	}

	@Test
	public void testIsTableExistsNull() throws Exception {
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(null);
		assertFalse(proxy.isTableExists("foo"));
		proxy.close();
	}
}
