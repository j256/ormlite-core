package com.j256.ormlite.support;

import static org.easymock.EasyMock.createMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;

public class BaseConnectionSourceTest extends BaseCoreTest {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void testBasicStuff() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		assertFalse(cs.isSavedConnection(createMock(DatabaseConnection.class)));
		DatabaseConnection conn = cs.getReadOnlyConnection(null);
		assertNotNull(conn);
		assertNull(cs.getSpecialConnection(null));
		cs.saveSpecialConnection(conn);
		assertSame(conn, cs.getSpecialConnection(null));
		assertTrue(cs.isSavedConnection(conn));
		assertFalse(cs.isSavedConnection(createMock(DatabaseConnection.class)));
		DatabaseConnection conn2 = cs.getReadOnlyConnection(null);
		assertSame(conn, conn2);
		assertNotNull(conn2);
		cs.clearSpecialConnection(conn);
		assertNull(cs.getSpecialConnection(null));
		assertFalse(cs.isSavedConnection(conn));
		assertNull(cs.getSavedConnection());
		cs.close();
	}

	@Test
	public void testNestedSave() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		DatabaseConnection conn = cs.getReadOnlyConnection(null);
		cs.saveSpecialConnection(conn);
		cs.saveSpecialConnection(conn);
		cs.clearSpecialConnection(conn);
		assertEquals(conn, cs.getSpecialConnection(null));
		cs.close();
	}

	@Test
	public void testSaveDifferentConnection() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		DatabaseConnection conn = cs.getReadOnlyConnection(null);
		cs.saveSpecialConnection(conn);
		assertThrowsExactly(SQLException.class, () -> {
			cs.saveSpecialConnection(createMock(DatabaseConnection.class));
		});
		cs.close();
	}

	@Test
	public void testClearNone() {
		OurConnectionSource cs = new OurConnectionSource();
		cs.clearSpecialConnection(createMock(DatabaseConnection.class));
		cs.close();
	}

	@Test
	public void testClearDifferentConnection() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		DatabaseConnection conn = cs.getReadOnlyConnection(null);
		cs.saveSpecialConnection(conn);
		cs.clearSpecialConnection(createMock(DatabaseConnection.class));
		cs.close();
	}

	private class OurConnectionSource extends BaseConnectionSource {

		@Override
		public DatabaseConnection getReadOnlyConnection(String tableName) {
			return getReadWriteConnection(tableName);
		}

		@Override
		public DatabaseConnection getReadWriteConnection(String tableName) {
			DatabaseConnection conn = getSavedConnection();
			if (conn == null) {
				return createMock(DatabaseConnection.class);
			} else {
				return conn;
			}
		}

		@Override
		public void releaseConnection(DatabaseConnection connection) {
			// noop
		}

		@Override
		public boolean saveSpecialConnection(DatabaseConnection connection) throws SQLException {
			return saveSpecial(connection);
		}

		@Override
		public void clearSpecialConnection(DatabaseConnection connection) {
			clearSpecial(connection, logger);
		}

		@Override
		public void close() {
			// noop
		}

		@Override
		public void closeQuietly() {
			// noop
		}

		@Override
		public DatabaseType getDatabaseType() {
			return databaseType;
		}

		@Override
		public boolean isOpen(String tableName) {
			return true;
		}

		@Override
		public boolean isSingleConnection(String tableName) {
			return true;
		}
	}
}
