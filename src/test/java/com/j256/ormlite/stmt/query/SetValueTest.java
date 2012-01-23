package com.j256.ormlite.stmt.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.UpdateBuilder;

public class SetValueTest extends BaseCoreTest {

	@Test
	public void testBasic() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		int val = 66654654;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(val, result.val);

		UpdateBuilder<Foo, Integer> up = dao.updateBuilder();
		int newVal = 165445654;
		up.updateColumnValue(Foo.VAL_COLUMN_NAME, newVal);
		assertEquals(1, dao.update(up.prepare()));

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(newVal, result.val);
	}

	@Test
	public void testSerializable() throws Exception {
		Dao<TestSerial, Integer> dao = createDao(TestSerial.class, true);
		TestSerial foo = new TestSerial();
		String stuff = "hjrjpe";
		foo.stuff = stuff;
		assertEquals(1, dao.create(foo));

		TestSerial result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(stuff, result.stuff);

		UpdateBuilder<TestSerial, Integer> up = dao.updateBuilder();
		String newStuff = "165445654";
		up.updateColumnValue("stuff", newStuff);
		assertEquals(1, dao.update(up.prepare()));

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(newStuff, result.stuff);
	}

	protected static class TestSerial {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(dataType = DataType.SERIALIZABLE)
		public Serializable stuff;
		public TestSerial() {
		}
	}
}
