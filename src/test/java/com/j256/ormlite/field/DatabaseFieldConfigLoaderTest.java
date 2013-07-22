package com.j256.ormlite.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Test;

public class DatabaseFieldConfigLoaderTest {

	private final static String FIELD_START = "# --field-start--\r\n";
	private final static String FIELD_END = "# --field-end--\r\n";

	@Test
	public void testConfigFile() throws Exception {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		StringBuilder body = new StringBuilder();
		StringWriter writer = new StringWriter();
		BufferedWriter buffer = new BufferedWriter(writer);

		String fieldName = "pwojfpweofjwefw";
		config.setFieldName(fieldName);
		body.append("fieldName=").append(fieldName).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		String columnName = "pwefw";
		config.setColumnName(columnName);
		body.append("columnName=").append(columnName).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		DataPersister dataPersister = DataType.BYTE_OBJ.getDataPersister();
		config.setDataPersister(dataPersister);
		body.append("dataPersister=").append(DataType.BYTE_OBJ).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		String defaultValue = "pwefw";
		config.setDefaultValue(defaultValue);
		body.append("defaultValue=").append(defaultValue).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		int width = 13212;
		config.setWidth(width);
		body.append("width=").append(width).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setCanBeNull(true);
		checkConfigOutput(config, body, writer, buffer);
		config.setCanBeNull(false);
		body.append("canBeNull=false\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setId(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setId(true);
		body.append("id=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setGeneratedId(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setGeneratedId(true);
		body.append("generatedId=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		String generatedIdSequence = "24332423";
		config.setGeneratedIdSequence(generatedIdSequence);
		body.append("generatedIdSequence=").append(generatedIdSequence).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setForeign(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setForeign(true);
		body.append("foreign=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setUseGetSet(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setUseGetSet(true);
		body.append("useGetSet=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		Enum<?> enumValue = OurEnum.FIRST;
		config.setUnknownEnumValue(enumValue);
		body.append("unknownEnumValue=")
				.append(enumValue.getClass().getName())
				.append('#')
				.append(enumValue)
				.append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setThrowIfNull(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setThrowIfNull(true);
		body.append("throwIfNull=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		/**
		 * persisted is skipped
		 */

		String format = "wpgjogwjpogwjp";
		config.setFormat(format);
		body.append("format=").append(format).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setUnique(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setUnique(true);
		body.append("unique=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setUniqueCombo(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setUniqueCombo(true);
		body.append("uniqueCombo=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		String indexName = "wfewjpwepjjp";
		config.setIndexName(indexName);
		body.append("indexName=").append(indexName).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		String uniqueIndexName = "w2254423fewjpwepjjp";
		config.setUniqueIndexName(uniqueIndexName);
		body.append("uniqueIndexName=").append(uniqueIndexName).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setForeignAutoRefresh(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setForeignAutoRefresh(true);
		body.append("foreignAutoRefresh=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		int maxForeign = 2112;
		config.setMaxForeignAutoRefreshLevel(maxForeign);
		body.append("maxForeignAutoRefreshLevel=").append(maxForeign).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		@SuppressWarnings("unchecked")
		Class<DataPersister> clazz = (Class<DataPersister>) DataType.CHAR.getDataPersister().getClass();
		config.setPersisterClass(clazz);
		body.append("persisterClass=").append(clazz.getName()).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setAllowGeneratedIdInsert(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setAllowGeneratedIdInsert(true);
		body.append("allowGeneratedIdInsert=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		String columnDefinition = "columnDef";
		config.setColumnDefinition(columnDefinition);
		body.append("columnDefinition=").append(columnDefinition).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setForeignAutoCreate(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setForeignAutoCreate(true);
		body.append("foreignAutoCreate=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setVersion(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setVersion(true);
		body.append("version=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		String foreignColumnName = "foreignNameOfColumn";
		config.setForeignColumnName(foreignColumnName);
		body.append("foreignColumnName=").append(foreignColumnName).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setReadOnly(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setReadOnly(true);
		body.append("readOnly=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		/*
		 * Test foreign collection
		 */

		config.setForeignCollection(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setForeignCollection(true);
		body.append("foreignCollection=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setForeignCollectionEager(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setForeignCollectionEager(true);
		body.append("foreignCollectionEager=true\r\n");
		checkConfigOutput(config, body, writer, buffer);

		int maxEager = 341;
		config.setForeignCollectionMaxEagerLevel(maxEager);
		body.append("foreignCollectionMaxEagerLevel=").append(maxEager).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		String columName = "jrgpgrjrwgpj";
		config.setForeignCollectionColumnName(columName);
		body.append("foreignCollectionColumnName=").append(columName).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		String foreignOrderColumn = "w225fwhi4jp";
		config.setForeignCollectionOrderColumnName(foreignOrderColumn);
		body.append("foreignCollectionOrderColumnName=").append(foreignOrderColumn).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);

		String foreignCollectionForeignFieldName = "ghiegerih";
		config.setForeignCollectionForeignFieldName(foreignCollectionForeignFieldName);
		body.append("foreignCollectionForeignFieldName=").append(foreignCollectionForeignFieldName).append("\r\n");
		checkConfigOutput(config, body, writer, buffer);
	}

	@Test
	public void testEmptyFile() throws Exception {
		String value = "";
		assertNull(DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value))));
	}

	@Test(expected = SQLException.class)
	public void testBadLine() throws Exception {
		String value = "not a good line";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	@Test
	public void testBlankLine() throws Exception {
		String value = "\r\n";
		assertNull(DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value))));
	}

	@Test
	public void testComment() throws Exception {
		String value = "# some comment\r\n";
		assertNull(DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadPersisterClass() throws Exception {
		String value = "persisterClass=unknown class name\r\n";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEnumValue() throws Exception {
		String value = "unknownEnumValue=notvalidclass\r\n";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEnumClass() throws Exception {
		String value = "unknownEnumValue=notvalidclass#somevalue\r\n";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEnumClassNotAnEnum() throws Exception {
		String value = "unknownEnumValue=java.lang.Object#somevalue\r\n";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEnumClassInvalidEnumValue() throws Exception {
		String value = "unknownEnumValue=" + OurEnum.class.getName() + "#notvalid\r\n";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	private enum OurEnum {
		FIRST,
		SECOND, ;
	}

	private static final String TABLE_NAME = "footable";

	private void checkConfigOutput(DatabaseFieldConfig config, StringBuilder body, StringWriter writer,
			BufferedWriter buffer) throws Exception {
		DatabaseFieldConfigLoader.write(buffer, config, TABLE_NAME);
		buffer.flush();
		StringBuilder output = new StringBuilder();
		output.append(FIELD_START).append(body).append(FIELD_END);
		assertEquals(output.toString(), writer.toString());
		StringReader reader = new StringReader(writer.toString());
		DatabaseFieldConfig configCopy = DatabaseFieldConfigLoader.fromReader(new BufferedReader(reader));
		assertTrue(isConfigEquals(config, configCopy));
		writer.getBuffer().setLength(0);
	}

	public static boolean isConfigEquals(DatabaseFieldConfig config1, DatabaseFieldConfig config2) {
		EqualsBuilder eb = new EqualsBuilder();
		eb.append(config1.getFieldName(), config2.getFieldName());
		eb.append(config1.getColumnName(), config2.getColumnName());
		eb.append(config1.getDataPersister(), config2.getDataPersister());
		eb.append(config1.getDefaultValue(), config2.getDefaultValue());
		eb.append(config1.getWidth(), config2.getWidth());
		eb.append(config1.isCanBeNull(), config2.isCanBeNull());
		eb.append(config1.isId(), config2.isId());
		eb.append(config1.isGeneratedId(), config2.isGeneratedId());
		eb.append(config1.getGeneratedIdSequence(), config2.getGeneratedIdSequence());
		eb.append(config1.isForeign(), config2.isForeign());
		eb.append(config1.getForeignTableConfig(), config2.getForeignTableConfig());
		eb.append(config1.isUseGetSet(), config2.isUseGetSet());
		eb.append(config1.getUnknownEnumValue(), config2.getUnknownEnumValue());
		eb.append(config1.isThrowIfNull(), config2.isThrowIfNull());
		eb.append(config1.isPersisted(), config2.isPersisted());
		eb.append(config1.getFormat(), config2.getFormat());
		eb.append(config1.isUnique(), config2.isUnique());
		eb.append(config1.isUniqueCombo(), config2.isUniqueCombo());
		eb.append(config1.getIndexName(TABLE_NAME), config2.getIndexName(TABLE_NAME));
		eb.append(config1.getUniqueIndexName(TABLE_NAME), config2.getUniqueIndexName(TABLE_NAME));
		eb.append(config1.isForeignAutoRefresh(), config2.isForeignAutoRefresh());
		eb.append(config1.getMaxForeignAutoRefreshLevel(), config2.getMaxForeignAutoRefreshLevel());
		eb.append(config1.getPersisterClass(), config2.getPersisterClass());
		eb.append(config1.isAllowGeneratedIdInsert(), config2.isAllowGeneratedIdInsert());
		eb.append(config1.getColumnDefinition(), config2.getColumnDefinition());
		eb.append(config1.isForeignAutoCreate(), config2.isForeignAutoCreate());
		eb.append(config1.isVersion(), config2.isVersion());
		// foreign collections
		eb.append(config1.isForeignCollection(), config2.isForeignCollection());
		eb.append(config1.isForeignCollectionEager(), config2.isForeignCollectionEager());
		eb.append(config1.getForeignCollectionOrderColumnName(), config2.getForeignCollectionOrderColumnName());
		eb.append(config1.getForeignCollectionMaxEagerLevel(), config2.getForeignCollectionMaxEagerLevel());
		eb.append(config1.getForeignCollectionForeignFieldName(), config2.getForeignCollectionForeignFieldName());
		return eb.isEquals();
	}
}
