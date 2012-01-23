package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.field.FieldType;

public class NullArgHolderTest {

	@Test
	public void testStuff() {
		NullArgHolder holder = new NullArgHolder();
		assertEquals("null-holder", holder.getColumnName());
		holder.setMetaInfo((String) null);
		holder.setMetaInfo((FieldType) null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetValueThrows() {
		new NullArgHolder().setValue(null);
	}
}
