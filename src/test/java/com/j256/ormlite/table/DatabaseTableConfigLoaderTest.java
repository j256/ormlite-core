package com.j256.ormlite.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Test;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.DatabaseFieldConfigLoader;
import com.j256.ormlite.table.DatabaseTableConfigTest.NoFields;

public class DatabaseTableConfigLoaderTest {

	private final static String LINE_SEP = System.getProperty("line.separator");

	private final static String TABLE_START = "# --table-start--" + LINE_SEP;
	private final static String TABLE_END = "# --table-end--" + LINE_SEP;
	private final static String TABLE_FIELDS_START = "# --table-fields-start--" + LINE_SEP;
	private final static String TABLE_FIELDS_END = "# --table-fields-end--" + LINE_SEP;

	@Test
	public void testConfigFile() throws Exception {
		DatabaseTableConfig<NoFields> config = new DatabaseTableConfig<NoFields>();
		StringBuilder body = new StringBuilder();
		StringWriter writer = new StringWriter();
		BufferedWriter buffer = new BufferedWriter(writer);

		Class<NoFields> clazz = NoFields.class;
		config.setDataClass(clazz);
		body.append("dataClass=").append(clazz.getName()).append(LINE_SEP);
		checkConfigOutput(config, body, writer, buffer, false);

		String tableName = "pojgefwpjoefwpjo";
		config.setTableName(tableName);
		body.append("tableName=").append(tableName).append(LINE_SEP);
		checkConfigOutput(config, body, writer, buffer, false);

		DatabaseFieldConfig field1 = new DatabaseFieldConfig();
		String columnName = "efjpowefpjoefw";
		field1.setColumnName(columnName);
		config.setFieldConfigs(Arrays.asList(field1));
		StringWriter fieldWriter = new StringWriter();
		BufferedWriter fieldBuffer = new BufferedWriter(fieldWriter);
		DatabaseFieldConfigLoader.write(fieldBuffer, field1, tableName);
		fieldBuffer.flush();
		body.append("# --table-fields-start--").append(LINE_SEP);
		body.append(fieldWriter.toString());
		checkConfigOutput(config, body, writer, buffer, true);
	}

	@Test
	public void testConfigEntriesFromStream() throws Exception {
		StringBuilder value = new StringBuilder();
		value.append(TABLE_START);
		value.append("# random comment").append(LINE_SEP);
		// blank line
		value.append(LINE_SEP);
		value.append("dataClass=").append(Foo.class.getName()).append(LINE_SEP);
		String tableName = "fprwojfgopwejfw";
		value.append("tableName=").append(tableName).append(LINE_SEP);
		value.append("# --table-fields-start--").append(LINE_SEP);
		value.append("# --field-start--").append(LINE_SEP);
		String fieldName = "weopjfwefjw";
		value.append("fieldName=").append(fieldName).append(LINE_SEP);
		value.append("canBeNull=true").append(LINE_SEP);
		value.append("generatedId=true").append(LINE_SEP);
		value.append("# --field-end--").append(LINE_SEP);
		value.append("# --table-fields-end--").append(LINE_SEP);
		value.append(TABLE_END);
		List<DatabaseTableConfig<?>> tables =
				DatabaseTableConfigLoader.loadDatabaseConfigFromReader(new BufferedReader(new StringReader(
						value.toString())));
		assertEquals(1, tables.size());
		assertEquals(tableName, tables.get(0).getTableName());
		DatabaseTableConfig<?> config = tables.get(0);
		List<DatabaseFieldConfig> fields = config.getFieldConfigs();
		assertEquals(1, fields.size());
		assertEquals(fieldName, fields.get(0).getFieldName());
	}

	@Test(expected = SQLException.class)
	public void testConfigInvalidLine() throws Exception {
		StringBuilder value = new StringBuilder();
		value.append(TABLE_START);
		value.append("dataClass").append(LINE_SEP);
		DatabaseTableConfigLoader.loadDatabaseConfigFromReader(new BufferedReader(new StringReader(value.toString())));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConfigUnknownClass() throws Exception {
		StringBuilder value = new StringBuilder();
		value.append(TABLE_START);
		value.append("dataClass=unknown.class.name.okay").append(LINE_SEP);
		value.append("# --table-fields-start--").append(LINE_SEP);
		value.append("# --field-start--").append(LINE_SEP);
		value.append("fieldName=xxx").append(LINE_SEP);
		value.append("# --field-end--").append(LINE_SEP);
		value.append("# --table-fields-end--").append(LINE_SEP);
		value.append(TABLE_END);
		DatabaseTableConfigLoader.loadDatabaseConfigFromReader(new BufferedReader(new StringReader(value.toString())));
	}

	@Test
	public void testQuickEndOfConfig() throws Exception {
		StringBuilder value = new StringBuilder();
		value.append(TABLE_START);
		value.append("dataClass=").append(Foo.class.getName()).append(LINE_SEP);
		value.append("# --table-fields-start--").append(LINE_SEP);
		value.append("# --field-start--").append(LINE_SEP);
		value.append("fieldName=xxx").append(LINE_SEP);
		value.append("# --field-end--").append(LINE_SEP);
		value.append("# --field-start--").append(LINE_SEP);
		List<DatabaseTableConfig<?>> tables =
				DatabaseTableConfigLoader.loadDatabaseConfigFromReader(new BufferedReader(new StringReader(
						value.toString())));
		assertEquals(1, tables.size());
		DatabaseTableConfig<?> config = tables.get(0);
		List<DatabaseFieldConfig> fields = config.getFieldConfigs();
		assertEquals(1, fields.size());
	}

	/* ======================================================================================= */

	private void checkConfigOutput(DatabaseTableConfig<?> config, StringBuilder body, StringWriter writer,
			BufferedWriter buffer, boolean hasFields) throws Exception {
		DatabaseTableConfigLoader.write(buffer, config);
		buffer.flush();
		StringBuilder output = new StringBuilder();
		output.append(TABLE_START).append(body);
		if (!hasFields) {
			output.append(TABLE_FIELDS_START);
		}
		output.append(TABLE_FIELDS_END);
		output.append(TABLE_END);
		assertEquals(output.toString(), writer.toString());
		StringReader reader = new StringReader(writer.toString());
		DatabaseTableConfig<?> configCopy = DatabaseTableConfigLoader.fromReader(new BufferedReader(reader));
		assertTrue(isConfigEquals(config, configCopy));
		writer.getBuffer().setLength(0);
	}

	private boolean isConfigEquals(DatabaseTableConfig<?> config1, DatabaseTableConfig<?> config2) {
		EqualsBuilder eb = new EqualsBuilder();
		eb.append(config1.getDataClass(), config2.getDataClass());
		eb.append(config1.getTableName(), config2.getTableName());
		return eb.isEquals();
	}

	protected static class Foo {
		int id;
		public Foo() {
		}
	}
}
