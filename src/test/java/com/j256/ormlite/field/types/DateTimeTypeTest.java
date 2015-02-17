package com.j256.ormlite.field.types;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.support.DatabaseResults;

public class DateTimeTypeTest {

	@Test(expected = SQLException.class)
	public void testJavaToSqlArg() throws Exception {
		DateTimeType.getSingleton().javaToSqlArg(null, new Object());
	}

	@Test
	public void testParseDefaultString() throws SQLException {
		Long value = 423424234234L;
		assertEquals(value, DateTimeType.getSingleton().parseDefaultString(null, value.toString()));
	}

	@Test(expected = SQLException.class)
	public void testResultToSqlArg() throws Exception {
		DatabaseResults results = createMock(DatabaseResults.class);
		int col = 21;
		long value = 2094234324L;
		expect(results.getLong(col)).andReturn(value);
		replay(results);
		DateTimeType.getSingleton().resultToJava(null, results, col);
	}
}
