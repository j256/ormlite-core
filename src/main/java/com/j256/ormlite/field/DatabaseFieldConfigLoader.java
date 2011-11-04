package com.j256.ormlite.field;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.field.types.VoidType;
import com.j256.ormlite.misc.SqlExceptionUtil;

/**
 * Database field configuration loader which reads and writes {@link DatabaseFieldConfig} from a text file/stream.
 * 
 * @author graywatson
 */
public class DatabaseFieldConfigLoader {

	private static final String CONFIG_FILE_START_MARKER = "# --field-start--";
	private static final String CONFIG_FILE_END_MARKER = "# --field-end--";

	private static final int DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL =
			DatabaseField.DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL;
	private static final int DEFAULT_MAX_EAGER_FOREIGN_COLLECTION_LEVEL =
			ForeignCollectionField.MAX_EAGER_FOREIGN_COLLECTION_LEVEL;
	private static final Class<? extends DataPersister> DEFAULT_PERSISTER_CLASS = VoidType.class;
	private static final DataPersister DEFAULT_DATA_PERSISTER = DataType.UNKNOWN.getDataPersister();
	private static final boolean DEFAULT_CAN_BE_NULL = true;

	/**
	 * Load a configuration in from a text file.
	 * 
	 * @return A config if any of the fields were set otherwise null on EOF.
	 */
	public static DatabaseFieldConfig fromReader(BufferedReader reader) throws SQLException {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		boolean anything = false;
		while (true) {
			String line;
			try {
				line = reader.readLine();
			} catch (IOException e) {
				throw SqlExceptionUtil.create("Could not read DatabaseFieldConfig from stream", e);
			}
			if (line == null) {
				break;
			}
			// we do this so we can support multiple class configs per file
			if (line.equals(CONFIG_FILE_END_MARKER)) {
				break;
			}
			// skip empty lines or comments
			if (line.length() == 0 || line.startsWith("#") || line.equals(CONFIG_FILE_START_MARKER)) {
				continue;
			}
			String[] parts = line.split("=", -2);
			if (parts.length != 2) {
				throw new SQLException("DatabaseFieldConfig reading from stream cannot parse line: " + line);
			}
			readField(config, parts[0], parts[1]);
			anything = true;
		}
		// if we got any config lines then we return the config
		if (anything) {
			return config;
		} else {
			// otherwise we return null for none
			return null;
		}
	}

	/**
	 * Write the configuration to a buffered writer.
	 */
	public static void write(BufferedWriter writer, DatabaseFieldConfig config, String tableName) throws SQLException {
		try {
			writeConfig(writer, config, tableName);
		} catch (IOException e) {
			throw SqlExceptionUtil.create("Could not write config to writer", e);
		}
	}

	private static final String FIELD_NAME_FIELD_NAME = "fieldName";
	private static final String FIELD_NAME_COLUMN_NAME = "columnName";
	private static final String FIELD_NAME_DATA_PERSISTER = "dataPersister";
	private static final String FIELD_NAME_DEFAULT_VALUE = "defaultValue";
	private static final String FIELD_NAME_WIDTH = "width";
	private static final String FIELD_NAME_CAN_BE_NULL = "canBeNull";
	private static final String FIELD_NAME_ID = "id";
	private static final String FIELD_NAME_GENERATED_ID = "generatedId";
	private static final String FIELD_NAME_GENERATED_ID_SEQUENCE = "generatedIdSequence";
	private static final String FIELD_NAME_FOREIGN = "foreign";
	private static final String FIELD_NAME_USE_GET_SET = "useGetSet";
	private static final String FIELD_NAME_UNKNOWN_ENUM_VALUE = "unknownEnumValue";
	private static final String FIELD_NAME_THROW_IF_NULL = "throwIfNull";
	private static final String FIELD_NAME_FORMAT = "format";
	private static final String FIELD_NAME_UNIQUE = "unique";
	private static final String FIELD_NAME_UNIQUE_COMBO = "uniqueCombo";
	private static final String FIELD_NAME_INDEX_NAME = "indexName";
	private static final String FIELD_NAME_UNIQUE_INDEX_NAME = "uniqueIndexName";
	private static final String FIELD_NAME_FOREIGN_AUTO_REFRESH = "foreignAutoRefresh";
	private static final String FIELD_NAME_MAX_FOREIGN_AUTO_REFRESH_LEVEL = "maxForeignAutoRefreshLevel";
	private static final String FIELD_NAME_FOREIGN_COLLECTION = "foreignCollection";
	private static final String FIELD_NAME_FOREIGN_COLLECTION_EAGER = "foreignCollectionEager";
	private static final String FIELD_NAME_FOREIGN_COLLECTION_ORDER_COLUMN = "foreignCollectionOrderColumn";
	private static final String FIELD_NAME_MAX_EAGER_FOREIGN_COLLECTION_LEVEL = "maxEagerForeignCollectionLevel";
	private static final String FIELD_NAME_PERSISTER_CLASS = "persisterClass";
	private static final String FIELD_NAME_ALLOW_GENERATED_ID_INSERT = "allowGeneratedIdInsert";
	private static final String FIELD_NAME_COLUMN_DEFINITION = "columnDefinition";
	private static final String FIELD_NAME_FOREIGN_AUTO_CREATE = "foreignAutoCreate";
	private static final String FIELD_NAME_VERSION = "version";

	/**
	 * Print the config to the writer.
	 */
	public static void writeConfig(BufferedWriter writer, DatabaseFieldConfig config, String tableName)
			throws IOException {
		writer.append(CONFIG_FILE_START_MARKER);
		writer.newLine();
		if (config.getFieldName() != null) {
			writer.append(FIELD_NAME_FIELD_NAME).append('=').append(config.getFieldName());
			writer.newLine();
		}
		if (config.getColumnName() != null) {
			writer.append(FIELD_NAME_COLUMN_NAME).append('=').append(config.getColumnName());
			writer.newLine();
		}
		if (config.getDataPersister() != DEFAULT_DATA_PERSISTER) {
			boolean found = false;
			for (DataType dataType : DataType.values()) {
				if (dataType.getDataPersister() == config.getDataPersister()) {
					writer.append(FIELD_NAME_DATA_PERSISTER).append('=').append(dataType.name());
					writer.newLine();
					found = true;
					break;
				}
			}
			if (!found) {
				throw new IllegalArgumentException("Unknown data persister field: " + config.getDataPersister());
			}
		}
		if (config.getDefaultValue() != null) {
			writer.append(FIELD_NAME_DEFAULT_VALUE).append('=').append(config.getDefaultValue());
			writer.newLine();
		}
		if (config.getWidth() != 0) {
			writer.append(FIELD_NAME_WIDTH).append('=').append(Integer.toString(config.getWidth()));
			writer.newLine();
		}
		if (config.isCanBeNull() != DEFAULT_CAN_BE_NULL) {
			writer.append(FIELD_NAME_CAN_BE_NULL).append('=').append(Boolean.toString(config.isCanBeNull()));
			writer.newLine();
		}
		if (config.isId()) {
			writer.append(FIELD_NAME_ID).append('=').append("true");
			writer.newLine();
		}
		if (config.isGeneratedId()) {
			writer.append(FIELD_NAME_GENERATED_ID).append('=').append("true");
			writer.newLine();
		}
		if (config.getGeneratedIdSequence() != null) {
			writer.append(FIELD_NAME_GENERATED_ID_SEQUENCE).append('=').append(config.getGeneratedIdSequence());
			writer.newLine();
		}
		if (config.isForeign()) {
			writer.append(FIELD_NAME_FOREIGN).append('=').append("true");
			writer.newLine();
		}
		if (config.isUseGetSet()) {
			writer.append(FIELD_NAME_USE_GET_SET).append('=').append("true");
			writer.newLine();
		}
		if (config.getUnknownEnumValue() != null) {
			writer.append(FIELD_NAME_UNKNOWN_ENUM_VALUE)
					.append('=')
					.append(config.getUnknownEnumValue().getClass().getName())
					.append("#")
					.append(config.getUnknownEnumValue().name());
			writer.newLine();
		}
		if (config.isThrowIfNull()) {
			writer.append(FIELD_NAME_THROW_IF_NULL).append('=').append("true");
			writer.newLine();
		}
		if (config.getFormat() != null) {
			writer.append(FIELD_NAME_FORMAT).append('=').append(config.getFormat());
			writer.newLine();
		}
		if (config.isUnique()) {
			writer.append(FIELD_NAME_UNIQUE).append('=').append("true");
			writer.newLine();
		}
		if (config.isUniqueCombo()) {
			writer.append(FIELD_NAME_UNIQUE_COMBO).append('=').append("true");
			writer.newLine();
		}
		String indexName = config.getIndexName(tableName);
		if (indexName != null) {
			writer.append(FIELD_NAME_INDEX_NAME).append('=').append(indexName);
			writer.newLine();
		}
		String uniqueIndexName = config.getUniqueIndexName(tableName);
		if (uniqueIndexName != null) {
			writer.append(FIELD_NAME_UNIQUE_INDEX_NAME).append('=').append(uniqueIndexName);
			writer.newLine();
		}
		if (config.isForeignAutoRefresh()) {
			writer.append(FIELD_NAME_FOREIGN_AUTO_REFRESH).append('=').append("true");
			writer.newLine();
		}
		if (config.getMaxForeignAutoRefreshLevel() != DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL) {
			writer.append(FIELD_NAME_MAX_FOREIGN_AUTO_REFRESH_LEVEL)
					.append('=')
					.append(Integer.toString(config.getMaxForeignAutoRefreshLevel()));
			writer.newLine();
		}
		if (config.getPersisterClass() != DEFAULT_PERSISTER_CLASS) {
			writer.append(FIELD_NAME_PERSISTER_CLASS).append('=').append(config.getPersisterClass().getName());
			writer.newLine();
		}
		if (config.isAllowGeneratedIdInsert()) {
			writer.append(FIELD_NAME_ALLOW_GENERATED_ID_INSERT).append('=').append("true");
			writer.newLine();
		}
		if (config.getColumnDefinition() != null) {
			writer.append(FIELD_NAME_COLUMN_DEFINITION).append('=').append(config.getColumnDefinition());
			writer.newLine();
		}
		if (config.isForeignAutoCreate()) {
			writer.append(FIELD_NAME_FOREIGN_AUTO_CREATE).append('=').append("true");
			writer.newLine();
		}
		if (config.isVersion()) {
			writer.append(FIELD_NAME_VERSION).append('=').append("true");
			writer.newLine();
		}

		/*
		 * Foreign collection settings:
		 */
		if (config.isForeignCollection()) {
			writer.append(FIELD_NAME_FOREIGN_COLLECTION).append('=').append("true");
			writer.newLine();
		}
		if (config.isForeignCollectionEager()) {
			writer.append(FIELD_NAME_FOREIGN_COLLECTION_EAGER).append('=').append("true");
			writer.newLine();
		}
		if (config.getForeignCollectionOrderColumn() != null) {
			writer.append(FIELD_NAME_FOREIGN_COLLECTION_ORDER_COLUMN)
					.append('=')
					.append(config.getForeignCollectionOrderColumn());
			writer.newLine();
		}
		if (config.getMaxEagerForeignCollectionLevel() != DEFAULT_MAX_EAGER_FOREIGN_COLLECTION_LEVEL) {
			writer.append(FIELD_NAME_MAX_EAGER_FOREIGN_COLLECTION_LEVEL)
					.append('=')
					.append(Integer.toString(config.getMaxEagerForeignCollectionLevel()));
			writer.newLine();
		}

		writer.append(CONFIG_FILE_END_MARKER);
		writer.newLine();
	}

	/**
	 * Set the configuration information for this field=value line.
	 */
	private static void readField(DatabaseFieldConfig config, String field, String value) {
		if (field.equals(FIELD_NAME_FIELD_NAME)) {
			config.setFieldName(value);
		} else if (field.equals(FIELD_NAME_COLUMN_NAME)) {
			config.setColumnName(value);
		} else if (field.equals(FIELD_NAME_DATA_PERSISTER)) {
			config.setDataPersister(DataType.valueOf(value).getDataPersister());
		} else if (field.equals(FIELD_NAME_DEFAULT_VALUE)) {
			config.setDefaultValue(value);
		} else if (field.equals(FIELD_NAME_WIDTH)) {
			config.setWidth(Integer.parseInt(value));
		} else if (field.equals(FIELD_NAME_CAN_BE_NULL)) {
			config.setCanBeNull(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_ID)) {
			config.setId(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_GENERATED_ID)) {
			config.setGeneratedId(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_GENERATED_ID_SEQUENCE)) {
			config.setGeneratedIdSequence(value);
		} else if (field.equals(FIELD_NAME_FOREIGN)) {
			config.setForeign(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_USE_GET_SET)) {
			config.setUseGetSet(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_UNKNOWN_ENUM_VALUE)) {
			String[] parts = value.split("#", -2);
			if (parts.length != 2) {
				throw new IllegalArgumentException(
						"Invalid value for unknownEnumvalue which should be in class#name format: " + value);
			}
			Class<?> enumClass;
			try {
				enumClass = Class.forName(parts[0]);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Unknown class specified for unknownEnumvalue: " + value);
			}
			Object[] consts = enumClass.getEnumConstants();
			if (consts == null) {
				throw new IllegalArgumentException("Invalid class is not an Enum for unknownEnumvalue: " + value);
			}
			@SuppressWarnings("rawtypes")
			Enum[] enumConstants = (Enum[]) consts;
			boolean found = false;
			for (Enum<?> enumInstance : enumConstants) {
				if (enumInstance.name().equals(parts[1])) {
					config.setUnknownEnumValue(enumInstance);
					found = true;
				}
			}
			if (!found) {
				throw new IllegalArgumentException("Invalid enum value name for unknownEnumvalue: " + value);
			}
		} else if (field.equals(FIELD_NAME_THROW_IF_NULL)) {
			config.setThrowIfNull(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_FORMAT)) {
			config.setFormat(value);
		} else if (field.equals(FIELD_NAME_UNIQUE)) {
			config.setUnique(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_UNIQUE_COMBO)) {
			config.setUniqueCombo(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_INDEX_NAME)) {
			config.setIndexName(value);
		} else if (field.equals(FIELD_NAME_UNIQUE_INDEX_NAME)) {
			config.setUniqueIndexName(value);
		} else if (field.equals(FIELD_NAME_FOREIGN_AUTO_REFRESH)) {
			config.setForeignAutoRefresh(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_MAX_FOREIGN_AUTO_REFRESH_LEVEL)) {
			config.setMaxForeignAutoRefreshLevel(Integer.parseInt(value));
		} else if (field.equals(FIELD_NAME_FOREIGN_COLLECTION)) {
			config.setForeignCollection(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_FOREIGN_COLLECTION_EAGER)) {
			config.setForeignCollectionEager(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_FOREIGN_COLLECTION_ORDER_COLUMN)) {
			config.setForeignCollectionOrderColumn(value);
		} else if (field.equals(FIELD_NAME_MAX_EAGER_FOREIGN_COLLECTION_LEVEL)) {
			config.setMaxEagerForeignCollectionLevel(Integer.parseInt(value));
		} else if (field.equals(FIELD_NAME_PERSISTER_CLASS)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends DataPersister> clazz = (Class<? extends DataPersister>) Class.forName(value);
				config.setPersisterClass(clazz);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Could not find persisterClass: " + value);
			}
		} else if (field.equals(FIELD_NAME_ALLOW_GENERATED_ID_INSERT)) {
			config.setAllowGeneratedIdInsert(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_COLUMN_DEFINITION)) {
			config.setColumnDefinition(value);
		} else if (field.equals(FIELD_NAME_FOREIGN_AUTO_CREATE)) {
			config.setForeignAutoCreate(Boolean.parseBoolean(value));
		} else if (field.equals(FIELD_NAME_VERSION)) {
			config.setVersion(Boolean.parseBoolean(value));
		}
	}
}
