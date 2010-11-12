package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;

public class DeleteBuilderTest extends BaseCoreTest {

	@Test
	public void testDeleteAll() throws Exception {
		DeleteBuilder<BaseFoo, String> stmtb = new DeleteBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}
}
