package com.j256.ormlite.support;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

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
		DatabaseConnection conn = cs.getReadOnlyConnection();
		assertNotNull(conn);
		assertNull(cs.getSpecialConnection());
		cs.saveSpecialConnection(conn);
		assertSame(conn, cs.getSpecialConnection());
		assertTrue(cs.isSavedConnection(conn));
		assertFalse(cs.isSavedConnection(createMock(DatabaseConnection.class)));
		DatabaseConnection conn2 = cs.getReadOnlyConnection();
		assertSame(conn, conn2);
		assertNotNull(conn2);
		cs.clearSpecialConnection(conn);
		assertNull(cs.getSpecialConnection());
		assertFalse(cs.isSavedConnection(conn));
		assertNull(cs.getSavedConnection());
		cs.close();
	}

	@Test
	public void testNestedSave() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		DatabaseConnection conn = cs.getReadOnlyConnection();
		cs.saveSpecialConnection(conn);
		cs.saveSpecialConnection(conn);
		cs.clearSpecialConnection(conn);
		assertEquals(conn, cs.getSpecialConnection());
		cs.close();
	}

	@Test(expected = SQLException.class)
	public void testSaveDifferentConnection() throws Exception {
		OurConnectionSource cs = new OurConnectionSource();
		DatabaseConnection conn = cs.getReadOnlyConnection();
		cs.saveSpecialConnection(conn);
		cs.saveSpecialConnection(createMock(DatabaseConnection.class));
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
		DatabaseConnection conn = cs.getReadOnlyConnection();
		cs.saveSpecialConnection(conn);
		cs.clearSpecialConnection(createMock(DatabaseConnection.class));
		cs.close();
	}

	private class OurConnectionSource extends BaseConnectionSource {

		public DatabaseConnection getReadOnlyConnection() {
			return getReadWriteConnection();
		}

		public DatabaseConnection getReadWriteConnection() {
			DatabaseConnection conn = getSavedConnection();
			if (conn == null) {
				return createMock(DatabaseConnection.class);
			} else {
				return conn;
			}
		}

		public void releaseConnection(DatabaseConnection connection) {
			// noop
		}

		public boolean saveSpecialConnection(DatabaseConnection connection) throws SQLException {
			return saveSpecial(connection);
		}

		public void clearSpecialConnection(DatabaseConnection connection) {
			clearSpecial(connection, logger);
		}

		public void close() {
			// noop
		}

		public void closeQuietly() {
			// noop
		}

		public DatabaseType getDatabaseType() {
			return databaseType;
		}

		public boolean isOpen() {
			return true;
		}

		public boolean isSingleConnection() {
			return true;
		}
	}
}
