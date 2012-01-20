package com.j256.ormlite.field.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class VoidTypeTest {

	@Test
	public void testStuff() throws Exception {
		VoidType voidType = new VoidType();
		assertNull(voidType.parseDefaultString(null, null));
		assertNull(voidType.resultToJava(null, null, 0));
		assertFalse(voidType.isValidForField(null));
	}
}
