package com.j256.ormlite.stmt;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class SelectArgTest extends BaseCoreStmtTest {

	@Test
	public void testGetBeforeSetValue() {
		SelectArg selectArg = new SelectArg();
		assertThrowsExactly(SQLException.class, () -> {
			selectArg.getSqlArgValue();
		});
	}

	@Test
	public void testSetValue() throws Exception {
		SelectArg selectArg = new SelectArg();
		Object foo = new Object();
		selectArg.setValue(foo);
		assertSame(foo, selectArg.getSqlArgValue());
	}

	@Test
	public void testSetNumber() throws Exception {
		SelectArg selectArg = new SelectArg();
		int val = 10;
		selectArg.setMetaInfo("val", numberFieldType);
		selectArg.setValue(val);
		assertSame(val, selectArg.getSqlArgValue());
	}

	@Test
	public void testGetColumnNameOk() {
		SelectArg selectArg = new SelectArg();
		String name = "fwewfwef";
		selectArg.setMetaInfo(name, stringFieldType);
		assertSame(name, selectArg.getColumnName());
	}

	@Test
	public void testGetColumnNameTwice() {
		SelectArg selectArg = new SelectArg();
		selectArg.setMetaInfo("1", numberFieldType);
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			selectArg.setMetaInfo("2", numberFieldType);
		});
	}

	@Test
	public void testSetNullValue() throws Exception {
		SelectArg selectArg = new SelectArg();
		selectArg.setValue(null);
		assertNull(selectArg.getSqlArgValue());
	}

	@Test
	public void testForeignValue() {
		SelectArg selectArg = new SelectArg();
		assertTrue(selectArg.toString().contains("[unset]"));
		Foo foo = new Foo();
		selectArg.setValue(foo);
		selectArg.setMetaInfo("id", foreignFieldType);
		assertTrue(selectArg.toString().contains(Integer.toString(foo.id)), selectArg + " wrong value");
	}

	@Test
	public void testToString() {
		SelectArg selectArg = new SelectArg();
		assertTrue(selectArg.toString().contains("[unset]"));
		selectArg.setValue(null);
		assertTrue(selectArg.toString().contains("[null]"));
		String value = "fwefefewf";
		selectArg.setValue(value);
		assertTrue(selectArg.toString().contains(value));
	}

	@Test
	public void testDoubleSet() {
		SelectArg selectArg = new SelectArg();
		selectArg.setMetaInfo("id", numberFieldType);
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			selectArg.setMetaInfo("id", stringFieldType);
		});
	}
}
