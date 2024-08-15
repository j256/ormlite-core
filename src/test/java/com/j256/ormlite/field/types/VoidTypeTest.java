package com.j256.ormlite.field.types;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class VoidTypeTest {

	@Test
	public void testStuff() throws Exception {
		VoidType voidType = new VoidType();
		assertNull(voidType.parseDefaultString(null, null));
		assertNull(voidType.resultToJava(null, null, 0));
		assertFalse(voidType.isValidForField(null));
	}
}
