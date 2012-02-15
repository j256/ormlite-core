package com.j256.ormlite.field;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.types.VoidType;
import com.j256.ormlite.misc.JavaxPersistence;
import com.j256.ormlite.table.DatabaseTableConfig;

/**
 * Database field configuration information either supplied by a {@link DatabaseField} annotation or by direct Java or
 * Spring wiring.
 * 
 * @author graywatson
 */
public class DatabaseFieldConfig {

	private static final int DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL =
			DatabaseField.DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL;
	private static final int DEFAULT_MAX_EAGER_FOREIGN_COLLECTION_LEVEL =
			ForeignCollectionField.MAX_EAGER_FOREIGN_COLLECTION_LEVEL;
	private static final Class<? extends DataPersister> DEFAULT_PERSISTER_CLASS = VoidType.class;
	private static final DataPersister DEFAULT_DATA_PERSISTER = DataType.UNKNOWN.getDataPersister();
	private static final boolean DEFAULT_CAN_BE_NULL = true;

	private String fieldName;
	private String columnName;
	private DataPersister dataPersister = DEFAULT_DATA_PERSISTER;
	private String defaultValue;
	private int width;
	private boolean canBeNull = DEFAULT_CAN_BE_NULL;
	private boolean id;
	private boolean generatedId;
	private String generatedIdSequence;
	private boolean foreign;
	private DatabaseTableConfig<?> foreignTableConfig;
	private boolean useGetSet;
	private Enum<?> unknownEnumValue;
	private boolean throwIfNull;
	private boolean persisted = true;
	private String format;
	private boolean unique;
	private boolean uniqueCombo;
	private boolean index;
	private String indexName;
	private boolean uniqueIndex;
	private String uniqueIndexName;
	private boolean foreignAutoRefresh;
	private int maxForeignAutoRefreshLevel = DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL;
	private boolean foreignCollection;
	private boolean foreignCollectionEager;
	private String foreignCollectionOrderColumn;
	private String foreignCollectionColumn;
	private int maxEagerForeignCollectionLevel = DEFAULT_MAX_EAGER_FOREIGN_COLLECTION_LEVEL;
	private Class<? extends DataPersister> persisterClass = DEFAULT_PERSISTER_CLASS;
	private boolean allowGeneratedIdInsert;
	private String columnDefinition;
	private boolean foreignAutoCreate;
	private boolean version;

	public DatabaseFieldConfig() {
		// for spring
	}

	public DatabaseFieldConfig(String fieldName) {
		this.fieldName = fieldName;
	}

	public DatabaseFieldConfig(String fieldName, String columnName, DataType dataType, String defaultValue, int width,
			boolean canBeNull, boolean id, boolean generatedId, String generatedIdSequence, boolean foreign,
			DatabaseTableConfig<?> foreignTableConfig, boolean useGetSet, Enum<?> unknownEnumValue,
			boolean throwIfNull, String format, boolean unique, String indexName, String uniqueIndexName,
			boolean autoRefresh, int maxForeignAutoRefreshLevel, int maxForeignCollectionLevel) {
		this(fieldName, columnName, (dataType == null ? null : dataType.getDataPersister()), defaultValue, width,
				canBeNull, id, generatedId, generatedIdSequence, foreign, foreignTableConfig, useGetSet,
				unknownEnumValue, throwIfNull, format, unique, indexName, uniqueIndexName, autoRefresh,
				maxForeignAutoRefreshLevel, maxForeignCollectionLevel);
	}

	public DatabaseFieldConfig(String fieldName, String columnName, DataPersister dataPersister, String defaultValue,
			int width, boolean canBeNull, boolean id, boolean generatedId, String generatedIdSequence, boolean foreign,
			DatabaseTableConfig<?> foreignTableConfig, boolean useGetSet, Enum<?> unknownEnumValue,
			boolean throwIfNull, String format, boolean unique, String indexName, String uniqueIndexName,
			boolean autoRefresh, int maxForeignAutoRefreshLevel, int maxForeignCollectionLevel) {
		this.fieldName = fieldName;
		this.columnName = columnName;
		this.dataPersister = dataPersister;
		this.defaultValue = defaultValue;
		this.width = width;
		this.canBeNull = canBeNull;
		this.id = id;
		this.generatedId = generatedId;
		this.generatedIdSequence = generatedIdSequence;
		this.foreign = foreign;
		this.foreignTableConfig = foreignTableConfig;
		this.useGetSet = useGetSet;
		this.unknownEnumValue = unknownEnumValue;
		this.throwIfNull = throwIfNull;
		this.format = format;
		this.unique = unique;
		this.indexName = indexName;
		this.uniqueIndexName = uniqueIndexName;
		this.foreignAutoRefresh = autoRefresh;
		this.maxForeignAutoRefreshLevel = maxForeignAutoRefreshLevel;
		this.maxEagerForeignCollectionLevel = maxForeignCollectionLevel;
	}

	/**
	 * Return the name of the field in the class.
	 */
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @see DatabaseField#columnName()
	 */
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * The name is historical.
	 */
	public void setDataType(DataType dataType) {
		this.dataPersister = dataType.getDataPersister();
	}

	/**
	 * The name is historical.
	 * 
	 * @see DatabaseField#dataType()
	 */
	public DataPersister getDataPersister() {
		return dataPersister;
	}

	/**
	 * The name is historical.
	 */
	public void setDataPersister(DataPersister dataPersister) {
		this.dataPersister = dataPersister;
	}

	/**
	 * @see DatabaseField#defaultValue()
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @see DatabaseField#width()
	 */
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @see DatabaseField#canBeNull()
	 */
	public boolean isCanBeNull() {
		return canBeNull;
	}

	public void setCanBeNull(boolean canBeNull) {
		this.canBeNull = canBeNull;
	}

	/**
	 * @see DatabaseField#id()
	 */
	public boolean isId() {
		return id;
	}

	public void setId(boolean id) {
		this.id = id;
	}

	/**
	 * @see DatabaseField#generatedId()
	 */
	public boolean isGeneratedId() {
		return generatedId;
	}

	public void setGeneratedId(boolean generatedId) {
		this.generatedId = generatedId;
	}

	/**
	 * @see DatabaseField#generatedIdSequence()
	 */
	public String getGeneratedIdSequence() {
		return generatedIdSequence;
	}

	public void setGeneratedIdSequence(String generatedIdSequence) {
		this.generatedIdSequence = generatedIdSequence;
	}

	/**
	 * @see DatabaseField#foreign()
	 */
	public boolean isForeign() {
		return foreign;
	}

	public void setForeign(boolean foreign) {
		this.foreign = foreign;
	}

	/**
	 * For a foreign class which does not use the {@link DatabaseField} annotations, you need to inject the table
	 * configuration.
	 */
	public DatabaseTableConfig<?> getForeignTableConfig() {
		return foreignTableConfig;
	}

	public void setForeignTableConfig(DatabaseTableConfig<?> foreignTableConfig) {
		this.foreignTableConfig = foreignTableConfig;
	}

	/**
	 * @see DatabaseField#useGetSet()
	 */
	public boolean isUseGetSet() {
		return useGetSet;
	}

	public void setUseGetSet(boolean useGetSet) {
		this.useGetSet = useGetSet;
	}

	/**
	 * @deprecated Switch to {@link #getUnknownEnumValue()}.
	 */
	@Deprecated
	public Enum<?> getUnknownEnumvalue() {
		return unknownEnumValue;
	}

	public Enum<?> getUnknownEnumValue() {
		return unknownEnumValue;
	}

	public void setUnknownEnumValue(Enum<?> unknownEnumValue) {
		this.unknownEnumValue = unknownEnumValue;
	}

	public boolean isThrowIfNull() {
		return throwIfNull;
	}

	public void setThrowIfNull(boolean throwIfNull) {
		this.throwIfNull = throwIfNull;
	}

	public boolean isPersisted() {
		return persisted;
	}

	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isUniqueCombo() {
		return uniqueCombo;
	}

	public void setUniqueCombo(boolean uniqueCombo) {
		this.uniqueCombo = uniqueCombo;
	}

	public void setIndex(boolean index) {
		this.index = index;
	}

	public String getIndexName(String tableName) {
		if (index && indexName == null) {
			indexName = findIndexName(tableName);
		}
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public void setUniqueIndex(boolean uniqueIndex) {
		this.uniqueIndex = uniqueIndex;
	}

	public String getUniqueIndexName(String tableName) {
		if (uniqueIndex && uniqueIndexName == null) {
			uniqueIndexName = findIndexName(tableName);
		}
		return uniqueIndexName;
	}

	public void setUniqueIndexName(String uniqueIndexName) {
		this.uniqueIndexName = uniqueIndexName;
	}

	public void setForeignAutoRefresh(boolean foreignAutoRefresh) {
		this.foreignAutoRefresh = foreignAutoRefresh;
	}

	public boolean isForeignAutoRefresh() {
		return foreignAutoRefresh;
	}

	public int getMaxForeignAutoRefreshLevel() {
		return maxForeignAutoRefreshLevel;
	}

	public void setMaxForeignAutoRefreshLevel(int maxForeignLevel) {
		this.maxForeignAutoRefreshLevel = maxForeignLevel;
	}

	public int getMaxEagerForeignCollectionLevel() {
		return maxEagerForeignCollectionLevel;
	}

	public void setMaxEagerForeignCollectionLevel(int maxEagerForeignCollectionLevel) {
		this.maxEagerForeignCollectionLevel = maxEagerForeignCollectionLevel;
	}

	public void setForeignCollection(boolean foreignCollection) {
		this.foreignCollection = foreignCollection;
	}

	public boolean isForeignCollection() {
		return foreignCollection;
	}

	public void setForeignCollectionEager(boolean foreignCollectionEager) {
		this.foreignCollectionEager = foreignCollectionEager;
	}

	public boolean isForeignCollectionEager() {
		return foreignCollectionEager;
	}

	public void setForeignCollectionOrderColumn(String foreignCollectionOrderColumn) {
		this.foreignCollectionOrderColumn = foreignCollectionOrderColumn;
	}

	public String getForeignCollectionOrderColumn() {
		return foreignCollectionOrderColumn;
	}

	public void setForeignCollectionColumn(String foreignCollectionColumn) {
		this.foreignCollectionColumn = foreignCollectionColumn;
	}

	public String getForeignCollectionColumn() {
		return foreignCollectionColumn;
	}

	public Class<? extends DataPersister> getPersisterClass() {
		return persisterClass;
	}

	public void setPersisterClass(Class<? extends DataPersister> persisterClass) {
		this.persisterClass = persisterClass;
	}

	public boolean isAllowGeneratedIdInsert() {
		return allowGeneratedIdInsert;
	}

	public void setAllowGeneratedIdInsert(boolean allowGeneratedIdInsert) {
		this.allowGeneratedIdInsert = allowGeneratedIdInsert;
	}

	public String getColumnDefinition() {
		return columnDefinition;
	}

	public void setColumnDefinition(String columnDefinition) {
		this.columnDefinition = columnDefinition;
	}

	public boolean isForeignAutoCreate() {
		return foreignAutoCreate;
	}

	public void setForeignAutoCreate(boolean foreignAutoCreate) {
		this.foreignAutoCreate = foreignAutoCreate;
	}

	public boolean isVersion() {
		return version;
	}

	public void setVersion(boolean version) {
		this.version = version;
	}

	/**
	 * Create and return a config converted from a {@link Field} that may have one of the following annotations:
	 * {@link DatabaseField}, {@link ForeignCollectionField}, or javax.persistence...
	 */
	public static DatabaseFieldConfig fromField(DatabaseType databaseType, String tableName, Field field)
			throws SQLException {

		// first we lookup the @DatabaseField annotation
		DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
		if (databaseField != null) {
			if (databaseField.persisted()) {
				return fromDatabaseField(databaseType, tableName, field, databaseField);
			} else {
				return null;
			}
		}

		// lastly we check for @ForeignCollectionField
		ForeignCollectionField foreignCollection = field.getAnnotation(ForeignCollectionField.class);
		if (foreignCollection != null) {
			return fromForeignCollection(databaseType, field, foreignCollection);
		}

		/*
		 * NOTE: to remove javax.persistence usage, comment the following lines out
		 */
		DatabaseFieldConfig config = JavaxPersistence.createFieldConfig(databaseType, field);

		// this can be null
		return config;
	}

	/**
	 * Find and return the appropriate getter method for field.
	 * 
	 * @return Get method or null if none found.
	 */
	public static Method findGetMethod(Field field, boolean throwExceptions) {
		String methodName = methodFromField(field, "get");
		Method fieldGetMethod;
		try {
			fieldGetMethod = field.getDeclaringClass().getMethod(methodName);
		} catch (Exception e) {
			if (throwExceptions) {
				throw new IllegalArgumentException("Could not find appropriate get method for " + field);
			} else {
				return null;
			}
		}
		if (fieldGetMethod.getReturnType() != field.getType()) {
			if (throwExceptions) {
				throw new IllegalArgumentException("Return type of get method " + methodName + " does not return "
						+ field.getType());
			} else {
				return null;
			}
		}
		return fieldGetMethod;
	}

	/**
	 * Find and return the appropriate setter method for field.
	 * 
	 * @return Set method or null if none found.
	 */
	public static Method findSetMethod(Field field, boolean throwExceptions) {
		String methodName = methodFromField(field, "set");
		Method fieldSetMethod;
		try {
			fieldSetMethod = field.getDeclaringClass().getMethod(methodName, field.getType());
		} catch (Exception e) {
			if (throwExceptions) {
				throw new IllegalArgumentException("Could not find appropriate set method for " + field);
			} else {
				return null;
			}
		}
		if (fieldSetMethod.getReturnType() != void.class) {
			if (throwExceptions) {
				throw new IllegalArgumentException("Return type of set method " + methodName + " returns "
						+ fieldSetMethod.getReturnType() + " instead of void");
			} else {
				return null;
			}
		}
		return fieldSetMethod;
	}

	public static DatabaseFieldConfig fromDatabaseField(DatabaseType databaseType, String tableName, Field field,
			DatabaseField databaseField) {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		config.fieldName = field.getName();
		if (databaseType.isEntityNamesMustBeUpCase()) {
			config.fieldName = config.fieldName.toUpperCase();
		}
		config.columnName = valueIfNotBlank(databaseField.columnName());
		DataType dataType = databaseField.dataType();
		if (dataType != null) {
			config.dataPersister = dataType.getDataPersister();
		}
		// NOTE: == did not work with the NO_DEFAULT string
		String defaultValue = databaseField.defaultValue();
		if (!defaultValue.equals(DatabaseField.DEFAULT_STRING)) {
			config.defaultValue = defaultValue;
		}
		config.width = databaseField.width();
		config.canBeNull = databaseField.canBeNull();
		config.id = databaseField.id();
		config.generatedId = databaseField.generatedId();
		config.generatedIdSequence = valueIfNotBlank(databaseField.generatedIdSequence());
		config.foreign = databaseField.foreign();
		config.useGetSet = databaseField.useGetSet();
		config.unknownEnumValue = findMatchingEnumVal(field, databaseField.unknownEnumName());
		config.throwIfNull = databaseField.throwIfNull();
		config.format = valueIfNotBlank(databaseField.format());
		config.unique = databaseField.unique();
		config.uniqueCombo = databaseField.uniqueCombo();

		// add in the index information
		config.index = databaseField.index();
		config.indexName = valueIfNotBlank(databaseField.indexName());
		config.uniqueIndex = databaseField.uniqueIndex();
		config.uniqueIndexName = valueIfNotBlank(databaseField.uniqueIndexName());
		config.foreignAutoRefresh = databaseField.foreignAutoRefresh();
		config.maxForeignAutoRefreshLevel = databaseField.maxForeignAutoRefreshLevel();
		config.persisterClass = databaseField.persisterClass();
		config.allowGeneratedIdInsert = databaseField.allowGeneratedIdInsert();
		config.columnDefinition = valueIfNotBlank(databaseField.columnDefinition());
		config.foreignAutoCreate = databaseField.foreignAutoCreate();
		config.version = databaseField.version();

		return config;
	}

	/**
	 * Internal method that finds the matching enum for a configured field that has the name argument.
	 * 
	 * @return The matching enum value or null if blank enum name.
	 * @throws IllegalArgumentException
	 *             If the enum name is not known.
	 */
	public static Enum<?> findMatchingEnumVal(Field field, String unknownEnumName) {
		if (unknownEnumName == null || unknownEnumName.length() == 0) {
			return null;
		}
		for (Enum<?> enumVal : (Enum<?>[]) field.getType().getEnumConstants()) {
			if (enumVal.name().equals(unknownEnumName)) {
				return enumVal;
			}
		}
		throw new IllegalArgumentException("Unknwown enum unknown name " + unknownEnumName + " for field " + field);
	}

	private static DatabaseFieldConfig fromForeignCollection(DatabaseType databaseType, Field field,
			ForeignCollectionField foreignCollection) {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		config.fieldName = field.getName();
		if (foreignCollection.columnName().length() > 0) {
			config.columnName = foreignCollection.columnName();
		}
		config.foreignCollection = true;
		config.foreignCollectionEager = foreignCollection.eager();
		config.maxEagerForeignCollectionLevel = foreignCollection.maxEagerForeignCollectionLevel();
		config.foreignCollectionOrderColumn = valueIfNotBlank(foreignCollection.orderColumnName());
		config.foreignCollectionColumn = valueIfNotBlank(foreignCollection.foreignColumnName());
		return config;
	}

	private String findIndexName(String tableName) {
		if (columnName == null) {
			return tableName + "_" + fieldName + "_idx";
		} else {
			return tableName + "_" + columnName + "_idx";
		}
	}

	private static String valueIfNotBlank(String newValue) {
		if (newValue == null || newValue.length() == 0) {
			return null;
		} else {
			return newValue;
		}
	}

	private static String methodFromField(Field field, String prefix) {
		return prefix + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
	}
}
