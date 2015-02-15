package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Test which demonstrates a bug to the create-or-update DAO call with custom ids.
 * 
 * @author lstrzelecki
 */
public class CreateOrUpdateTest extends BaseCoreTest {

	{
		DataPersisterManager.registerDataPersisters(EntityIdType.getInstance());
	}

	@Test
	public void shouldStoreEntityWithStringIdAndLoadFromDbOnCreate() throws Exception {
		Dao<EntityWithStringId, String> dao = createDao(EntityWithStringId.class, true);

		EntityWithStringId data = new EntityWithStringId();
		data.id = "generated_id_from_factory";
		data.value = "some value";
		dao.create(data);

		EntityWithStringId found = dao.queryForId(data.id);
		assertNotNull(found);
		assertEquals(found.id, data.id);
		assertEquals(found.value, data.value);
	}

	@Test
	public void shouldStoreEntityWithStringIdAndLoadFromDbOnCreateOrUpdate() throws Exception {
		Dao<EntityWithStringId, String> dao = createDao(EntityWithStringId.class, true);

		EntityWithStringId data = new EntityWithStringId();
		data.id = "generated_id_from_factory";
		data.value = "some value";
		dao.createOrUpdate(data);

		EntityWithStringId found = dao.queryForId(data.id);
		assertNotNull(found);
		assertEquals(found.id, data.id);
		assertEquals(found.value, data.value);
	}

	@Test
	public void shouldStoreEntityWithCustomTypeIdAndLoadFromDbOnCreate() throws Exception {
		Dao<EntityWithCustomTypeId, EntityId> dao = createDao(EntityWithCustomTypeId.class, true);

		EntityWithCustomTypeId data = new EntityWithCustomTypeId();
		data.id = EntityId.entityId("generated_id_from_factory");
		data.value = "some value";
		dao.create(data);

		EntityWithCustomTypeId found = dao.queryForId(data.id);
		assertNotNull(found);
		assertEquals(found.id, data.id);
		assertEquals(found.value, data.value);
	}

	@Test
	public void shouldStoreEntityWithCustomTypeIdAndLoadFromDbOnCreateOrUpdate() throws Exception {
		Dao<EntityWithCustomTypeId, EntityId> dao = createDao(EntityWithCustomTypeId.class, true);

		EntityWithCustomTypeId data = new EntityWithCustomTypeId();
		data.id = EntityId.entityId("generated_id_from_factory");
		data.value = "some value";
		dao.createOrUpdate(data);

		EntityWithCustomTypeId found = dao.queryForId(data.id);
		assertNotNull(found);
		assertEquals(found.id, data.id);
		assertEquals(found.value, data.value);

	}

	@DatabaseTable
	static class EntityWithStringId {
		@DatabaseField(id = true)
		String id;

		@DatabaseField
		String value;
	}

	@DatabaseTable
	static class EntityWithCustomTypeId {
		@DatabaseField(id = true)
		EntityId id;

		@DatabaseField
		String value;
	}

	/**
	 * Special entity class to test id.
	 */
	static class EntityId {

		private String value;

		private EntityId(String value) {
			this.value = value;
		}

		static EntityId entityId(String value) {
			return new EntityId(value);
		}

		@Override
		public boolean equals(Object other) {
			if (other == null || getClass() != other.getClass()) {
				return false;
			}
			EntityId entityId = (EntityId) other;
			return value.equals(entityId.value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}
	}

	/**
	 * Custom ID type.
	 */
	static class EntityIdType extends StringType {

		private static final EntityIdType instance = new EntityIdType();

		public static EntityIdType getInstance() {
			return instance;
		}

		private EntityIdType() {
			super(SqlType.STRING, new Class<?>[] { EntityId.class });
		}

		@Override
		public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
			EntityId entityId = (EntityId) javaObject;
			return entityId.value;
		}

		@Override
		public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
			return EntityId.entityId((String) sqlArg);
		}
	}
}
