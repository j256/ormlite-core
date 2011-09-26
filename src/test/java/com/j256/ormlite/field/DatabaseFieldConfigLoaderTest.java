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

	private final static String FIELD_START = "# --field-start--\n";
	private final static String FIELD_END = "# --field-end--\n";

	@Test
	public void testConfigFile() throws Exception {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		StringBuilder body = new StringBuilder();
		StringWriter writer = new StringWriter();
		BufferedWriter buffer = new BufferedWriter(writer);

		String fieldName = "pwojfpweofjwefw";
		config.setFieldName(fieldName);
		body.append("fieldName=").append(fieldName).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		String columnName = "pwefw";
		config.setColumnName(columnName);
		body.append("columnName=").append(columnName).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		DataPersister dataPersister = DataType.BYTE_OBJ.getDataPersister();
		config.setDataPersister(dataPersister);
		body.append("dataPersister=").append(DataType.BYTE_OBJ).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		String defaultValue = "pwefw";
		config.setDefaultValue(defaultValue);
		body.append("defaultValue=").append(defaultValue).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		int width = 13212;
		config.setWidth(width);
		body.append("width=").append(width).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setCanBeNull(true);
		checkConfigOutput(config, body, writer, buffer);
		config.setCanBeNull(false);
		body.append("canBeNull=false").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setId(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setId(true);
		body.append("id=true").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setGeneratedId(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setGeneratedId(true);
		body.append("generatedId=true").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		String generatedIdSequence = "24332423";
		config.setGeneratedIdSequence(generatedIdSequence);
		body.append("generatedIdSequence=").append(generatedIdSequence).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setForeign(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setForeign(true);
		body.append("foreign=true").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setUseGetSet(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setUseGetSet(true);
		body.append("useGetSet=true").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		Enum<?> enumValue = OurEnum.FIRST;
		config.setUnknownEnumValue(enumValue);
		body.append("unknownEnumValue=")
				.append(enumValue.getClass().getName())
				.append('#')
				.append(enumValue)
				.append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setThrowIfNull(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setThrowIfNull(true);
		body.append("throwIfNull=true").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		String format = "wpgjogwjpogwjp";
		config.setFormat(format);
		body.append("format=").append(format).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setUnique(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setUnique(true);
		body.append("unique=true").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setUniqueCombo(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setUniqueCombo(true);
		body.append("uniqueCombo=true").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		String indexName = "wfewjpwepjjp";
		config.setIndexName(indexName);
		body.append("indexName=").append(indexName).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		String uniqueIndexName = "w2254423fewjpwepjjp";
		config.setUniqueIndexName(uniqueIndexName);
		body.append("uniqueIndexName=").append(uniqueIndexName).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setForeignAutoRefresh(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setForeignAutoRefresh(true);
		body.append("foreignAutoRefresh=true").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		int maxForeign = 2112;
		config.setMaxForeignAutoRefreshLevel(maxForeign);
		body.append("maxForeignAutoRefreshLevel=").append(maxForeign).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setForeignCollection(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setForeignCollection(true);
		body.append("foreignCollection=true").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setForeignCollectionEager(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setForeignCollectionEager(true);
		body.append("foreignCollectionEager=true").append("\n");
		checkConfigOutput(config, body, writer, buffer);

		String foreignOrderColumn = "w225fwhi4jp";
		config.setForeignCollectionOrderColumn(foreignOrderColumn);
		body.append("foreignCollectionOrderColumn=").append(foreignOrderColumn).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		int maxEager = 341;
		config.setMaxEagerForeignCollectionLevel(maxEager);
		body.append("maxEagerForeignCollectionLevel=").append(maxEager).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		@SuppressWarnings("unchecked")
		Class<DataPersister> clazz = (Class<DataPersister>) DataType.CHAR.getDataPersister().getClass();
		config.setPersisterClass(clazz);
		body.append("persisterClass=").append(clazz.getName()).append("\n");
		checkConfigOutput(config, body, writer, buffer);

		config.setAllowGeneratedIdInsert(false);
		checkConfigOutput(config, body, writer, buffer);
		config.setAllowGeneratedIdInsert(true);
		body.append("allowGeneratedIdInsert=true").append("\n");
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
		String value = "\n";
		assertNull(DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value))));
	}

	@Test
	public void testComment() throws Exception {
		String value = "# some comment\n";
		assertNull(DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadPersisterClass() throws Exception {
		String value = "persisterClass=unknown class name\n";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEnumValue() throws Exception {
		String value = "unknownEnumValue=notvalidclass\n";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEnumClass() throws Exception {
		String value = "unknownEnumValue=notvalidclass#somevalue\n";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEnumClassNotAnEnum() throws Exception {
		String value = "unknownEnumValue=java.lang.Object#somevalue\n";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadEnumClassInvalidEnumValue() throws Exception {
		String value = "unknownEnumValue=" + OurEnum.class.getName() + "#notvalid\n";
		DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(value)));
	}

	private enum OurEnum {
		FIRST,
		SECOND, ;
	}

	private void checkConfigOutput(DatabaseFieldConfig config, StringBuilder body, StringWriter writer,
			BufferedWriter buffer) throws Exception {
		DatabaseFieldConfigLoader.write(buffer, config);
		buffer.flush();
		StringBuilder output = new StringBuilder();
		output.append(FIELD_START).append(body).append(FIELD_END);
		assertEquals(output.toString(), writer.toString());
		StringReader reader = new StringReader(writer.toString());
		DatabaseFieldConfig configCopy = DatabaseFieldConfigLoader.fromReader(new BufferedReader(reader));
		assertTrue(isConfigEquals(config, configCopy));
		writer.getBuffer().setLength(0);
	}

	private boolean isConfigEquals(DatabaseFieldConfig config1, DatabaseFieldConfig config2) {
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
		eb.append(config1.getFormat(), config2.getFormat());
		eb.append(config1.isUnique(), config2.isUnique());
		eb.append(config1.isUniqueCombo(), config2.isUniqueCombo());
		eb.append(config1.getIndexName(), config2.getIndexName());
		eb.append(config1.getUniqueIndexName(), config2.getUniqueIndexName());
		eb.append(config1.isForeignAutoRefresh(), config2.isForeignAutoRefresh());
		eb.append(config1.getMaxForeignAutoRefreshLevel(), config2.getMaxForeignAutoRefreshLevel());
		eb.append(config1.isForeignCollection(), config2.isForeignCollection());
		eb.append(config1.isForeignCollectionEager(), config2.isForeignCollectionEager());
		eb.append(config1.getForeignCollectionOrderColumn(), config2.getForeignCollectionOrderColumn());
		eb.append(config1.getMaxEagerForeignCollectionLevel(), config2.getMaxEagerForeignCollectionLevel());
		eb.append(config1.getPersisterClass(), config2.getPersisterClass());
		eb.append(config1.isAllowGeneratedIdInsert(), config2.isAllowGeneratedIdInsert());
		return eb.isEquals();
	}
}
