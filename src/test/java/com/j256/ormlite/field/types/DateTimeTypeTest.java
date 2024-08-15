package com.j256.ormlite.field.types;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.support.DatabaseResults;

public class DateTimeTypeTest {

	@Test
	public void testJavaToSqlArg() {
		assertThrowsExactly(SQLException.class, () -> {
			DateTimeType.getSingleton().javaToSqlArg(null, new Object());
		});
	}

	@Test
	public void testParseDefaultString() throws SQLException {
		Long value = 423424234234L;
		assertEquals(value, DateTimeType.getSingleton().parseDefaultString(null, value.toString()));
	}

	@Test
	public void testResultToSqlArg() throws Exception {
		DatabaseResults results = createMock(DatabaseResults.class);
		int col = 21;
		long value = 2094234324L;
		expect(results.getLong(col)).andReturn(value);
		replay(results);
		assertThrowsExactly(SQLException.class, () -> {
			DateTimeType.getSingleton().resultToJava(null, results, col);
		});
	}
}
