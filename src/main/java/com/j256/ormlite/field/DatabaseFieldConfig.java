package com.j256.ormlite.field;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.misc.JavaxPersistence;
import com.j256.ormlite.table.DatabaseTableConfig;

/**
 * Database field configuration information either supplied by a {@link DatabaseField} annotation or by direct Java or
 * Spring wiring.
 * 
 * @author graywatson
 */
public class DatabaseFieldConfig {

	private String fieldName;
	private String columnName;
	private DataPersister dataPersister;
	private String defaultValue;
	private int width;
	private boolean canBeNull;
	private boolean id;
	private boolean generatedId;
	private String generatedIdSequence;
	private boolean foreign;
	private DatabaseTableConfig<?> foreignTableConfig;
	private boolean useGetSet;
	private Enum<?> unknownEnumvalue;
	private boolean throwIfNull;
	private String format;
	private boolean unique;
	private boolean uniqueCombo;
	private String indexName;
	private String uniqueIndexName;
	private boolean foreignAutoRefresh;
	private int maxForeignAutoRefreshLevel = DatabaseField.MAX_FOREIGN_AUTO_REFRESH_LEVEL;
	private boolean foreignCollection;
	private boolean foreignCollectionEager;
	private int maxEagerForeignCollectionLevel = ForeignCollectionField.MAX_EAGER_FOREIGN_COLLECTION_LEVEL;
	private Class<? extends DataPersister> persisterClass;

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
		this.unknownEnumvalue = unknownEnumValue;
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

	public Enum<?> getUnknownEnumvalue() {
		return unknownEnumvalue;
	}

	public void setUnknownEnumvalue(Enum<?> unknownEnumvalue) {
		this.unknownEnumvalue = unknownEnumvalue;
	}

	public boolean isThrowIfNull() {
		return throwIfNull;
	}

	public void setThrowIfNull(boolean throwIfNull) {
		this.throwIfNull = throwIfNull;
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

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getUniqueIndexName() {
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

	public Class<? extends DataPersister> getPersisterClass() {
		return persisterClass;
	}

	/**
	 * Create and return a config converted from a {@link Field} that may have either a {@link DatabaseField} annotation
	 * or the javax.persistence annotations.
	 */
	public static DatabaseFieldConfig fromField(DatabaseType databaseType, String tableName, Field field)
			throws SQLException {
		// first we lookup the DatabaseField annotation
		DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
		if (databaseField != null) {
			if (databaseField.persisted()) {
				return fromDatabaseField(databaseType, tableName, field, databaseField);
			} else {
				return null;
			}
		}

		ForeignCollectionField foreignCollection = field.getAnnotation(ForeignCollectionField.class);
		if (foreignCollection != null) {
			return fromForeignCollection(databaseType, tableName, field, foreignCollection);
		}

		/*
		 * NOTE: to remove javax.persistence usage, comment the following lines out
		 */
		DatabaseFieldConfig config = JavaxPersistence.createFieldConfig(databaseType, field);
		if (config != null) {
			return config;
		}

		return null;
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

	@SuppressWarnings("deprecation")
	private static DatabaseFieldConfig fromDatabaseField(DatabaseType databaseType, String tableName, Field field,
			DatabaseField databaseField) {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		config.fieldName = field.getName();
		if (databaseType.isEntityNamesMustBeUpCase()) {
			config.fieldName = config.fieldName.toUpperCase();
		}
		if (databaseField.columnName().length() > 0) {
			config.columnName = databaseField.columnName();
		} else {
			config.columnName = null;
		}
		if (databaseField.dataType() == null) {
			config.dataPersister = null;
		} else {
			config.dataPersister = databaseField.dataType().getDataPersister();
		}
		// NOTE: == did not work with the NO_DEFAULT string
		if (databaseField.defaultValue().equals(DatabaseField.NO_DEFAULT)) {
			config.defaultValue = null;
		} else {
			config.defaultValue = databaseField.defaultValue();
		}
		config.width = databaseField.width();
		config.canBeNull = databaseField.canBeNull();
		config.id = databaseField.id();
		config.generatedId = databaseField.generatedId();
		if (databaseField.generatedIdSequence().length() > 0) {
			config.generatedIdSequence = databaseField.generatedIdSequence();
		} else {
			config.generatedIdSequence = null;
		}
		config.foreign = databaseField.foreign();
		config.useGetSet = databaseField.useGetSet();
		if (databaseField.unknownEnumName().length() > 0) {
			config.unknownEnumvalue = findMatchingEnumVal(field, databaseField.unknownEnumName());
		} else {
			config.unknownEnumvalue = null;
		}
		config.throwIfNull = databaseField.throwIfNull();
		if (databaseField.format().length() > 0) {
			config.format = databaseField.format();
		} else {
			config.format = null;
		}
		config.unique = databaseField.unique();
		config.uniqueCombo = databaseField.uniqueCombo();

		// add in the index information
		config.indexName = findIndexName(tableName, databaseField.indexName(), databaseField.index(), config);
		config.uniqueIndexName =
				findIndexName(tableName, databaseField.uniqueIndexName(), databaseField.uniqueIndex(), config);
		config.foreignAutoRefresh = databaseField.foreignAutoRefresh();
		if (databaseField.maxForeignLevel() != DatabaseField.MAX_FOREIGN_AUTO_REFRESH_LEVEL) {
			config.maxForeignAutoRefreshLevel = databaseField.maxForeignLevel();
		} else {
			config.maxForeignAutoRefreshLevel = databaseField.maxForeignAutoRefreshLevel();
		}
		config.persisterClass = databaseField.persisterClass();

		return config;
	}

	private static DatabaseFieldConfig fromForeignCollection(DatabaseType databaseType, String tableName, Field field,
			ForeignCollectionField foreignCollection) {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		config.fieldName = field.getName();
		if (foreignCollection.columnName().length() > 0) {
			config.columnName = foreignCollection.columnName();
		} else {
			config.columnName = field.getName();
		}
		config.foreignCollection = true;
		config.foreignCollectionEager = foreignCollection.eager();
		config.maxEagerForeignCollectionLevel = foreignCollection.maxEagerForeignCollectionLevel();
		return config;
	}

	private static String findIndexName(String tableName, String indexName, boolean index, DatabaseFieldConfig config) {
		if (indexName.length() > 0) {
			return indexName;
		} else if (index) {
			if (config.columnName == null) {
				return tableName + "_" + config.fieldName + "_idx";
			} else {
				return tableName + "_" + config.columnName + "_idx";
			}
		} else {
			return null;
		}
	}

	private static String methodFromField(Field field, String prefix) {
		return prefix + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
	}

	private static Enum<?> findMatchingEnumVal(Field field, String unknownEnumName) {
		for (Enum<?> enumVal : (Enum<?>[]) field.getType().getEnumConstants()) {
			if (enumVal.name().equals(unknownEnumName)) {
				return enumVal;
			}
		}
		throw new IllegalArgumentException("Unknwown enum unknown name " + unknownEnumName + " for field " + field);
	}
}
