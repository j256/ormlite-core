package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DeleteBuilderTest extends BaseCoreStmtTest {

	@Test
	public void testDeleteAll() throws Exception {
		DeleteBuilder<Foo, String> stmtb = new DeleteBuilder<Foo, String>(databaseType, baseFooTableInfo);
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}
}
