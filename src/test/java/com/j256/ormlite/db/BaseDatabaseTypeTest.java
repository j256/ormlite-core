package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.TestUtils;

public class BaseDatabaseTypeTest {

	private final DatabaseType databaseType = new StubDatabaseType();

	@Test
	public void testBaseDatabaseType() throws Exception {
		assertEquals("-- ", databaseType.getCommentLinePrefix());
		String word = "word";
		assertEquals("'" + word + "'", TestUtils.appendEscapedWord(databaseType, word));
	}

	@Test
	public void testLoadDriver() throws Exception {
		databaseType.loadDriver();
	}

	@Test
	public void testCreateTableReturnsZero() {
		assertTrue(databaseType.isCreateTableReturnsZero());
	}

	private class StubDatabaseType extends BaseDatabaseType {
		public String getDriverClassName() {
			return "java.lang.String";
		}
		public String getDriverUrlPart() {
			return "foo";
		}
	}
}
