package com.j256.ormlite.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.TableInfo;

public abstract class BaseCoreStmtTest extends BaseCoreTest {

	protected TableInfo<Foo, Integer> baseFooTableInfo;
	protected TableInfo<SchemaFoo, Integer> baseSchemaFooTableInfo;
	protected FieldType numberFieldType;
	protected FieldType stringFieldType;
	protected FieldType foreignFieldType;

	@Override
	@BeforeEach
	public void before() throws Exception {
		super.before();

		Field field = Foo.class.getDeclaredField("stringField");
		assertEquals(String.class, field.getType());
		stringFieldType = FieldType.createFieldType(databaseType, "BaseFoo", field, Foo.class);
		stringFieldType.configDaoInformation(connectionSource, Foo.class);
		field = Foo.class.getDeclaredField("val");
		assertEquals(int.class, field.getType());
		numberFieldType = FieldType.createFieldType(databaseType, "BaseFoo", field, Foo.class);
		numberFieldType.configDaoInformation(connectionSource, Foo.class);
		field = Foreign.class.getDeclaredField("foo");
		assertEquals(Foo.class, field.getType());
		foreignFieldType = FieldType.createFieldType(databaseType, "BaseFoo", field, Foreign.class);
		foreignFieldType.configDaoInformation(connectionSource, Foreign.class);

		baseFooTableInfo = new TableInfo<Foo, Integer>(databaseType, Foo.class);
		baseSchemaFooTableInfo = new TableInfo<SchemaFoo, Integer>(databaseType, SchemaFoo.class);
	}
}
