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
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(supported, proxy.isAutoCommitSupported());
		verify(conn);
	}

	@Test
	public void testIsAutoCommitSupportedNull() throws Exception {
		assertFalse(new DatabaseConnectionProxy(null).isAutoCommitSupported());
	}

	@Test
	public void testIsAutoCommit() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		boolean autoCommit = false;
		expect(conn.isAutoCommit()).andReturn(autoCommit);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(autoCommit, proxy.isAutoCommit());
		verify(conn);
	}

	@Test
	public void testIsAutoCommitNull() throws Exception {
		assertFalse(new DatabaseConnectionProxy(null).isAutoCommit());
	}

	@Test
	public void testSetAutoCommit() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		boolean autoCommit = false;
		conn.setAutoCommit(autoCommit);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.setAutoCommit(autoCommit);
		verify(conn);
	}

	@Test
	public void testSetAutoCommitNull() throws Exception {
		new DatabaseConnectionProxy(null).setAutoCommit(false);
	}

	@Test
	public void testSetSavePoint() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String name = "savepoint";
		expect(conn.setSavePoint(name)).andReturn(null);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.setSavePoint(name);
		verify(conn);
	}

	@Test
	public void testSetSavePointNull() throws Exception {
		assertNull(new DatabaseConnectionProxy(null).setSavePoint("name"));
	}

	@Test
	public void testCommit() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		conn.commit(null);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.commit(null);
		verify(conn);
	}

	@Test
	public void testCommitNull() throws Exception {
		new DatabaseConnectionProxy(null).commit(null);
	}

	@Test
	public void testRollback() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		conn.rollback(null);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.rollback(null);
		verify(conn);
	}

	@Test
	public void testRollbackNull() throws Exception {
		new DatabaseConnectionProxy(null).rollback(null);
	}

	@Test
	public void testExecuteStatement() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "select foo from bar";
		int result = 1312321;
		expect(conn.executeStatement(statement, 0)).andReturn(result);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.executeStatement(statement, 0));
		verify(conn);
	}

	@Test
	public void testExecuteStatementNull() throws Exception {
		assertEquals(0, new DatabaseConnectionProxy(null).executeStatement("statment", 0));
	}

	@Test
	public void testCompileStatementStringStatementTypeFieldTypeArrayInt() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "select foo from bar";
		StatementType type = StatementType.DELETE;
		int flags = 11253123;
		expect(conn.compileStatement(statement, type, null, flags)).andReturn(null);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.compileStatement(statement, type, null, flags);
		verify(conn);
	}

	@Test
	public void testCompileStatementStringStatementTypeFieldTypeArrayIntNull() throws Exception {
		assertNull(new DatabaseConnectionProxy(null).compileStatement("statment", StatementType.DELETE, null, 0));
	}

	@Test
	public void testInsert() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "insert bar";
		int result = 13712321;
		expect(conn.insert(statement, null, null, null)).andReturn(result);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.insert(statement, null, null, null));
		verify(conn);
	}

	@Test
	public void testInsertNull() throws Exception {
		assertEquals(0, new DatabaseConnectionProxy(null).insert("statment", null, null, null));
	}

	@Test
	public void testUpdate() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "insert bar";
		int result = 13212321;
		expect(conn.update(statement, null, null)).andReturn(result);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.update(statement, null, null));
		verify(conn);
	}

	@Test
	public void testUpdateNull() throws Exception {
		assertEquals(0, new DatabaseConnectionProxy(null).update("statment", null, null));
	}

	@Test
	public void testDelete() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "insert bar";
		int result = 13872321;
		expect(conn.delete(statement, null, null)).andReturn(result);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.delete(statement, null, null));
		verify(conn);
	}

	@Test
	public void testDeleteNull() throws Exception {
		assertEquals(0, new DatabaseConnectionProxy(null).delete("statment", null, null));
	}

	@Test
	public void testQueryForOne() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "insert bar";
		Object result = new Object();
		expect(conn.queryForOne(statement, null, null, null, null)).andReturn(result);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.queryForOne(statement, null, null, null, null));
		verify(conn);
	}

	@Test
	public void testQueryForOneNull() throws Exception {
		assertNull(new DatabaseConnectionProxy(null).queryForOne("statment", null, null, null, null));
	}

	@Test
	public void testQueryForLongString() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "select stuff from foo";
		long result = 31231231241414L;
		expect(conn.queryForLong(statement)).andReturn(result);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.queryForLong(statement));
		verify(conn);
	}

	@Test
	public void testQueryForLongStringNull() throws Exception {
		assertEquals(0, new DatabaseConnectionProxy(null).queryForLong("statment"));
	}

	@Test
	public void testQueryForLongStringObjectArrayFieldTypeArray() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		String statement = "select stuff from foo";
		long result = 3123123124141413L;
		expect(conn.queryForLong(statement, null, null)).andReturn(result);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(result, proxy.queryForLong(statement, null, null));
		verify(conn);
	}

	@Test
	public void testQueryForLongStringObjectArrayFieldTypeArrayNull() throws Exception {
		assertEquals(0, new DatabaseConnectionProxy(null).queryForLong("statment", null, null));
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
	public void testCloseQuietly() {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		conn.closeQuietly();
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		proxy.closeQuietly();
		verify(conn);
	}

	@Test
	public void testCloseQuietlyNull() {
		new DatabaseConnectionProxy(null).closeQuietly();
	}

	@Test
	public void testIsClosed() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		boolean closed = true;
		expect(conn.isClosed()).andReturn(closed);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(closed, proxy.isClosed());
		verify(conn);
	}

	@Test
	public void testIsClosedNull() throws Exception {
		assertTrue(new DatabaseConnectionProxy(null).isClosed());
	}

	@Test
	public void testIsTableExists() throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		boolean tableExists = true;
		String tableName = "fjewfjwef";
		expect(conn.isTableExists(tableName)).andReturn(tableExists);
		DatabaseConnectionProxy proxy = new DatabaseConnectionProxy(conn);
		replay(conn);
		assertEquals(tableExists, proxy.isTableExists(tableName));
		verify(conn);
	}

	@Test
	public void testIsTableExistsNull() throws Exception {
		assertFalse(new DatabaseConnectionProxy(null).isTableExists("foo"));
	}
}
