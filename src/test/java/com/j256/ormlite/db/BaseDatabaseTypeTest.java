package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.TestUtils;

public class BaseDatabaseTypeTest extends BaseOrmLiteTest {

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
}
