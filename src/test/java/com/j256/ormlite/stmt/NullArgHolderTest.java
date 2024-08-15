package com.j256.ormlite.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.field.FieldType;

public class NullArgHolderTest {

	@Test
	public void testStuff() {
		NullArgHolder holder = new NullArgHolder();
		assertEquals("null-holder", holder.getColumnName());
		holder.setMetaInfo((String) null);
		holder.setMetaInfo((FieldType) null);
	}

	@Test
	public void testSetValueThrows() {
		assertThrowsExactly(UnsupportedOperationException.class, () -> {
			new NullArgHolder().setValue(null);
		});
	}
}
