package com.j256.ormlite.field;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.BaseForeignCollection;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.EagerForeignCollection;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.LazyForeignCollection;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.stmt.mapped.MappedQueryForId;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableInfo;

/**
 * Per field information configured from the {@link DatabaseField} annotation and the associated {@link Field} in the
 * class. Use the {@link #createFieldType} static method to instantiate the class.
 * 
 * @author graywatson
 */
public class FieldType {

	/** default suffix added to fields that are id fields of foreign objects */
	public static final String FOREIGN_ID_FIELD_SUFFIX = "_id";
	public static final int MAX_FOREIGN_RECURSE_LEVEL = 10;

	// default values
	private static boolean DEFAULT_VALUE_BOOLEAN;
	private static byte DEFAULT_VALUE_BYTE;
	private static char DEFAULT_VALUE_CHAR;
	private static short DEFAULT_VALUE_SHORT;
	private static int DEFAULT_VALUE_INT;
	private static long DEFAULT_VALUE_LONG;
	private static float DEFAULT_VALUE_FLOAT;
	private static double DEFAULT_VALUE_DOUBLE;

	private final ConnectionSource connectionSource;
	private final String tableName;
	private final Field field;
	private final String fieldName;
	private final String dbColumnName;
	private final DataType dataType;
	private final Object dataTypeConfigObj;
	private final Object defaultValue;
	private final DatabaseFieldConfig fieldConfig;
	private final boolean isId;
	private final boolean isGeneratedId;
	private final String generatedIdSequence;
	private final FieldConverter fieldConverter;
	private final TableInfo<?, ?> foreignTableInfo;
	private final FieldType foreignFieldType;
	private final Dao<?, ?> foreignDao;
	private final MappedQueryForId<Object, Object> mappedQueryForId;
	private final Method fieldGetMethod;
	private final Method fieldSetMethod;

	/**
	 * You should use {@link FieldType#createFieldType} to instantiate one of these field if you have a {@link Field}.
	 * 
	 * @param recurseLevel
	 *            is used to make sure we done get in an infinite recursive loop if a foreign object refers to itself.
	 */
	public FieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig,
			Class<?> parentClass, int recurseLevel) throws SQLException {
		this.connectionSource = connectionSource;
		DatabaseType databaseType = connectionSource.getDatabaseType();
		this.tableName = tableName;
		this.field = field;
		this.fieldName = field.getName();
		Class<?> clazz = field.getType();
		DataType dataType;
		if (fieldConfig.getDataType() == DataType.UNKNOWN) {
			dataType = DataType.lookupClass(clazz);
		} else {
			dataType = fieldConfig.getDataType();
			if (!dataType.isValidForType(clazz)) {
				throw new IllegalArgumentException("Field class " + clazz + " for field " + this
						+ " is not valid for data type " + dataType);
			}
		}
		String defaultFieldName = field.getName();
		if (fieldConfig.isForeign() || fieldConfig.isForeignAutoRefresh()) {
			if (recurseLevel < MAX_FOREIGN_RECURSE_LEVEL) {
				if (dataType.isPrimitive()) {
					throw new IllegalArgumentException("Field " + this + " is a primitive class " + clazz
							+ " but marked as foreign");
				}
				DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();
				if (tableConfig == null) {
					tableConfig = DatabaseTableConfig.fromClass(connectionSource, clazz, recurseLevel + 1);
				} else {
					tableConfig.extractFieldTypes(connectionSource);
				}
				/*
				 * If we have a BaseDaoEnabled class then we generate the DAO and gets it's table information which has
				 * been set with the DAO. This seems like an extraneous DAO construction but it will be cached by the
				 * DaoManager and it is needed to be part of the TableInfo so it can be set on the resulting objects.
				 */
				if (BaseDaoEnabled.class.isAssignableFrom(clazz)) {
					BaseDaoImpl<?, ?> dao = (BaseDaoImpl<?, ?>) DaoManager.createDao(connectionSource, tableConfig);
					this.foreignTableInfo = dao.getTableInfo();
				} else {
					@SuppressWarnings({ "unchecked", "rawtypes" })
					TableInfo<?, ?> foreignInfo = new TableInfo(databaseType, null, tableConfig);
					this.foreignTableInfo = foreignInfo;
				}
				if (this.foreignTableInfo.getIdField() == null) {
					throw new IllegalArgumentException("Foreign field " + clazz + " does not have id field");
				}
				defaultFieldName = defaultFieldName + FOREIGN_ID_FIELD_SUFFIX;
				// this field's data type is the foreign id's type
				dataType = this.foreignTableInfo.getIdField().getDataType();
				if (fieldConfig.isForeignAutoRefresh()) {
					@SuppressWarnings("unchecked")
					MappedQueryForId<Object, Object> castMappedQueryForId =
							(MappedQueryForId<Object, Object>) MappedQueryForId.build(databaseType,
									this.foreignTableInfo);
					this.mappedQueryForId = castMappedQueryForId;
				} else {
					this.mappedQueryForId = null;
				}
			} else {
				// act like it's not foreign
				this.foreignTableInfo = null;
				this.mappedQueryForId = null;
			}
			this.foreignDao = null;
			this.foreignFieldType = null;
		} else if (fieldConfig.isForeignCollection() && recurseLevel == 0) {
			if (clazz != Collection.class && !ForeignCollection.class.isAssignableFrom(clazz)) {
				throw new SQLException("Field class for '" + field.getName() + "' must be of class "
						+ ForeignCollection.class.getSimpleName() + " or Collection.");
			}
			Type type = field.getGenericType();
			if (!(type instanceof ParameterizedType)) {
				throw new SQLException("Field class for '" + field.getName() + "' must be a parameterized Collection.");
			}
			Type[] genericArguments = ((ParameterizedType) type).getActualTypeArguments();
			if (genericArguments.length == 0) {
				// i doubt this will ever be reached
				throw new SQLException("Field class for '" + field.getName()
						+ "' must be a parameterized Collection with at least 1 type.");
			}
			clazz = (Class<?>) genericArguments[0];
			DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();
			if (tableConfig == null) {
				tableConfig = DatabaseTableConfig.fromClass(connectionSource, clazz, recurseLevel + 1);
			} else {
				tableConfig.extractFieldTypes(connectionSource);
			}

			FieldType foreignFieldType = null;
			for (FieldType fieldType : tableConfig.getFieldTypes(databaseType)) {
				if (fieldType.getFieldType() == parentClass) {
					foreignFieldType = fieldType;
					break;
				}
			}
			if (foreignFieldType == null) {
				throw new SQLException("Foreign collection object " + clazz + " for field '" + field.getName()
						+ "' does not contain a foreign field of class " + parentClass);
			}
			if (!foreignFieldType.isForeign()) {
				// this may never be reached
				throw new SQLException("Foreign collection object " + clazz + " for field '" + field.getName()
						+ "' contains a field of class " + parentClass + " but it's not foreign");
			}
			// we have to do this because of maven compile failures otherwise
			@SuppressWarnings("unchecked")
			Dao<Object, Object> otherDao = (Dao<Object, Object>) DaoManager.createDao(connectionSource, tableConfig);
			this.foreignDao = otherDao;
			this.foreignFieldType = foreignFieldType;
			this.foreignTableInfo = null;
			this.mappedQueryForId = null;
		} else if (dataType == DataType.UNKNOWN && (!fieldConfig.isForeignCollection())) {
			if (byte[].class.isAssignableFrom(clazz)) {
				throw new SQLException("ORMLite can't store unknown class " + clazz + " for field '" + field.getName()
						+ "'. byte[] fields must specify dataType=DataType.BYTE_ARRAY or SERIALIZABLE");
			} else if (Serializable.class.isAssignableFrom(clazz)) {
				throw new SQLException("ORMLite can't store unknown class " + clazz + " for field '" + field.getName()
						+ "'. Serializable fields must specify dataType=DataType.SERIALIZABLE");
			} else {
				throw new IllegalArgumentException("ORMLite does not know how to store field class " + clazz
						+ " for field " + this);
			}
		} else {
			this.foreignTableInfo = null;
			this.mappedQueryForId = null;
			this.foreignDao = null;
			this.foreignFieldType = null;
		}
		if (fieldConfig.getColumnName() == null) {
			this.dbColumnName = defaultFieldName;
		} else {
			this.dbColumnName = fieldConfig.getColumnName();
		}
		this.dataType = dataType;
		this.fieldConfig = fieldConfig;
		if (fieldConfig.isId()) {
			if (fieldConfig.isGeneratedId() || fieldConfig.getGeneratedIdSequence() != null) {
				throw new IllegalArgumentException("Must specify one of id, generatedId, and generatedIdSequence with "
						+ field.getName());
			}
			this.isId = true;
			this.isGeneratedId = false;
			this.generatedIdSequence = null;
		} else if (fieldConfig.isGeneratedId()) {
			if (fieldConfig.getGeneratedIdSequence() != null) {
				throw new IllegalArgumentException("Must specify one of id, generatedId, and generatedIdSequence with "
						+ field.getName());
			}
			this.isId = true;
			this.isGeneratedId = true;
			if (databaseType.isIdSequenceNeeded()) {
				this.generatedIdSequence = databaseType.generateIdSequenceName(tableName, this);
			} else {
				this.generatedIdSequence = null;
			}
		} else if (fieldConfig.getGeneratedIdSequence() != null) {
			this.isId = true;
			this.isGeneratedId = true;
			String seqName = fieldConfig.getGeneratedIdSequence();
			if (databaseType.isEntityNamesMustBeUpCase()) {
				seqName = seqName.toUpperCase();
			}
			this.generatedIdSequence = seqName;
		} else {
			this.isId = false;
			this.isGeneratedId = false;
			this.generatedIdSequence = null;
		}
		if (this.isGeneratedId && !this.dataType.isValidGeneratedType()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Generated-id field '").append(field.getName());
			sb.append("' in ").append(field.getDeclaringClass().getSimpleName());
			sb.append(" can't be type ").append(this.dataType);
			sb.append(".  Must be one of: ");
			for (DataType type : DataType.values()) {
				if (type.isValidGeneratedType()) {
					sb.append(type).append(' ');
				}
			}
			throw new IllegalArgumentException(sb.toString());
		}
		this.fieldConverter = databaseType.getFieldConverter(dataType);
		if (this.isId && foreignTableInfo != null) {
			throw new IllegalArgumentException("Id field " + field.getName() + " cannot also be a foreign object");
		}
		if (fieldConfig.isUseGetSet()) {
			this.fieldGetMethod = DatabaseFieldConfig.findGetMethod(field, true);
			this.fieldSetMethod = DatabaseFieldConfig.findSetMethod(field, true);
		} else {
			this.fieldGetMethod = null;
			this.fieldSetMethod = null;
		}
		if (fieldConfig.isThrowIfNull() && !dataType.isPrimitive()) {
			throw new SQLException("Field " + field.getName() + " must be a primitive if set with throwIfNull");
		}
		if (this.isId && !dataType.isAppropriateId()) {
			throw new SQLException("Field '" + field.getName() + "' is of data type " + dataType
					+ " which cannot be the ID field");
		}
		this.dataTypeConfigObj = dataType.makeConfigObject(this);
		String defaultStr = fieldConfig.getDefaultValue();
		if (defaultStr == null || defaultStr.equals("")) {
			this.defaultValue = null;
		} else if (this.isGeneratedId) {
			throw new SQLException("Field '" + field.getName() + "' cannot be a generatedId and have a default value '"
					+ defaultStr + "'");
		} else {
			this.defaultValue = this.fieldConverter.parseDefaultString(this, defaultStr);
		}
	}

	public String getTableName() {
		return tableName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Class<?> getFieldType() {
		return field.getType();
	}

	public String getDbColumnName() {
		return dbColumnName;
	}

	public DataType getDataType() {
		return dataType;
	}

	public Object getDataTypeConfigObj() {
		return dataTypeConfigObj;
	}

	public SqlType getSqlType() {
		return fieldConverter.getSqlType();
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public int getWidth() {
		return fieldConfig.getWidth();
	}

	public boolean isCanBeNull() {
		return fieldConfig.isCanBeNull();
	}

	/**
	 * Return whether the field is an id field. It is an id if {@link DatabaseField#id},
	 * {@link DatabaseField#generatedId}, OR {@link DatabaseField#generatedIdSequence} are enabled.
	 */
	public boolean isId() {
		return isId;
	}

	/**
	 * Return whether the field is a generated-id field. This is true if {@link DatabaseField#generatedId} OR
	 * {@link DatabaseField#generatedIdSequence} are enabled.
	 */
	public boolean isGeneratedId() {
		return isGeneratedId;
	}

	/**
	 * Return whether the field is a generated-id-sequence field. This is true if
	 * {@link DatabaseField#generatedIdSequence} is specified OR if {@link DatabaseField#generatedId} is enabled and the
	 * {@link DatabaseType#isIdSequenceNeeded} is enabled. If the latter is true then the sequence name will be
	 * auto-generated.
	 */
	public boolean isGeneratedIdSequence() {
		return generatedIdSequence != null;
	}

	/**
	 * Return the generated-id-sequence associated with the field or null if {@link #isGeneratedIdSequence} is false.
	 */
	public String getGeneratedIdSequence() {
		return generatedIdSequence;
	}

	public boolean isForeign() {
		return foreignTableInfo != null;
	}

	/**
	 * Return the {@link TableInfo} associated with the foreign object if the {@link DatabaseField#foreign()} annotation
	 * was set to true or null if none.
	 */
	TableInfo<?, ?> getForeignTableInfo() {
		return foreignTableInfo;
	}

	/**
	 * Assign to the data object the val corresponding to the fieldType.
	 */
	public void assignField(Object data, Object val) throws SQLException {
		// if this is a foreign object then val is the foreign object's id val
		if (foreignTableInfo != null && val != null) {
			// get the current field value which is the foreign-id
			Object foreignId = extractJavaFieldValue(data);
			/*
			 * See if we don't need to create a new foreign object. If we are refreshing and the id field has not
			 * changed then there is no need to create a new foreign object and maybe lose previously refreshed field
			 * information.
			 */
			if (foreignId != null && foreignId.equals(val)) {
				return;
			}
			Object foreignObject;
			if (mappedQueryForId == null) {
				// create a shell and assign its id field
				foreignObject = foreignTableInfo.createObject();
				foreignTableInfo.getIdField().assignField(foreignObject, val);
			} else {
				// do we need to auto-refresh the field?
				DatabaseConnection databaseConnection = connectionSource.getReadOnlyConnection();
				try {
					foreignObject = mappedQueryForId.execute(databaseConnection, val);
				} finally {
					connectionSource.releaseConnection(databaseConnection);
				}
			}
			// the value we are to assign to our field is now the foreign object itself
			val = foreignObject;
		}

		if (fieldSetMethod == null) {
			boolean accessible = field.isAccessible();
			if (!accessible) {
				field.setAccessible(true);
			}
			try {
				field.set(data, val);
			} catch (IllegalArgumentException e) {
				throw SqlExceptionUtil.create("Could not assign object '" + val + "' to field " + this, e);
			} catch (IllegalAccessException e) {
				throw SqlExceptionUtil.create("Could not assign object '" + val + "' to field " + this, e);
			} finally {
				if (!accessible) {
					// restore the accessibility of the field
					field.setAccessible(false);
				}
			}
		} else {
			try {
				fieldSetMethod.invoke(data, val);
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not call " + fieldSetMethod + " on object with '" + val + "' for "
						+ this, e);
			}
		}
	}
	/**
	 * Assign an ID value to this field.
	 */
	public Object assignIdValue(Object data, Number val) throws SQLException {
		Object idVal = dataType.convertIdNumber(val);
		if (idVal == null) {
			throw new SQLException("Invalid class " + dataType + " for sequence-id " + this);
		} else {
			assignField(data, idVal);
			return idVal;
		}
	}

	/**
	 * Return the value from the field in the object that is defined by this FieldType.
	 */
	public <FV> FV extractJavaFieldValue(Object object) throws SQLException {
		Object val;
		if (fieldGetMethod == null) {
			boolean accessible = field.isAccessible();
			try {
				if (!accessible) {
					field.setAccessible(true);
				}
				// field object may not be a T yet
				val = field.get(object);
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not get field value for " + this, e);
			} finally {
				if (!accessible) {
					field.setAccessible(false);
				}
			}
		} else {
			try {
				val = fieldGetMethod.invoke(object);
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not call " + fieldGetMethod + " for " + this, e);
			}
		}

		if (val == null) {
			return null;
		}

		// if this is a foreign object then we want its id field
		if (foreignTableInfo != null) {
			val = foreignTableInfo.getIdField().extractJavaFieldValue(val);
		}

		@SuppressWarnings("unchecked")
		FV converted = (FV) val;
		return converted;
	}

	/**
	 * Extract a field from an object and convert to something suitable to be passed to SQL as an argument.
	 */
	public Object extractJavaFieldToSqlArgValue(Object object) throws SQLException {
		return convertJavaFieldToSqlArgValue(extractJavaFieldValue(object));
	}

	/**
	 * Convert a field value to something suitable to be stored in the database.
	 */
	public <FV> FV convertJavaFieldToSqlArgValue(Object fieldVal) throws SQLException {
		if (fieldVal == null) {
			return null;
		} else {
			fieldVal = fieldConverter.javaToSqlArg(this, fieldVal);
			@SuppressWarnings("unchecked")
			FV converted = (FV) fieldVal;
			return converted;
		}
	}

	/**
	 * Return the id field associated with the foreign object or null if none.
	 */
	public FieldType getForeignIdField() throws SQLException {
		if (foreignTableInfo == null) {
			return null;
		} else {
			return foreignTableInfo.getIdField();
		}
	}

	/**
	 * Call through to {@link DataType#isEscapedValue()}
	 */
	public boolean isEscapedValue() {
		return dataType.isEscapedValue();
	}

	Enum<?> getUnknownEnumVal() {
		return fieldConfig.getUnknownEnumvalue();
	}

	/**
	 * Return the format of the field.
	 */
	String getFormat() {
		return fieldConfig.getFormat();
	}

	public boolean isUnique() {
		return fieldConfig.isUnique();
	}

	public String getIndexName() {
		return fieldConfig.getIndexName();
	}

	public String getUniqueIndexName() {
		return fieldConfig.getUniqueIndexName();
	}

	/**
	 * Call through to {@link DataType#isEscapedDefaultValue()}
	 */
	public boolean isEscapedDefaultValue() {
		return dataType.isEscapedDefaultValue();
	}

	/**
	 * Call through to {@link DataType#isComparable()}
	 */
	public boolean isComparable() {
		return dataType.isComparable();
	}

	/**
	 * Call through to {@link DataType#isSelectArgRequired()}
	 */
	public boolean isSelectArgRequired() {
		return dataType.isSelectArgRequired();
	}

	/**
	 * Call through to {@link DatabaseFieldConfig#isForeignCollection()}
	 */
	public boolean isForeignCollection() {
		return fieldConfig.isForeignCollection();
	}

	/**
	 * Build a foreign collection based on the field settings that matches the id argument.
	 */
	public <FT, FID> BaseForeignCollection<FT, FID> buildForeignCollection(Object id) throws SQLException {
		BaseForeignCollection<FT, FID> collection;
		@SuppressWarnings("unchecked")
		Dao<FT, FID> castDao = (Dao<FT, FID>) foreignDao;
		if (fieldConfig.isForeignCollectionEager()) {
			collection = new EagerForeignCollection<FT, FID>(castDao, foreignFieldType.dbColumnName, id);
		} else {
			collection = new LazyForeignCollection<FT, FID>(castDao, foreignFieldType.dbColumnName, id);
		}
		return collection;
	}

	/**
	 * Get the result object from the results. A call through to {@link FieldConverter#resultToJava}.
	 */
	public <T> T resultToJava(DatabaseResults results, Map<String, Integer> columnPositions) throws SQLException {
		Integer dbColumnPos = columnPositions.get(dbColumnName);
		if (dbColumnPos == null) {
			dbColumnPos = results.findColumn(dbColumnName);
			columnPositions.put(dbColumnName, dbColumnPos);
		}
		@SuppressWarnings("unchecked")
		T converted = (T) fieldConverter.resultToJava(this, results, dbColumnPos);
		if (dataType.isPrimitive()) {
			if (fieldConfig.isThrowIfNull() && results.wasNull(dbColumnPos)) {
				throw new SQLException("Results value for primitive field '" + fieldName
						+ "' was an invalid null value");
			}
		} else if (!fieldConverter.isStreamType() && results.wasNull(dbColumnPos)) {
			// we can't check if we have a null if this is a stream type
			return null;
		}
		return converted;
	}

	/**
	 * Call through to {@link DataType#isSelfGeneratedId()}
	 */
	public boolean isSelfGeneratedId() {
		return dataType.isSelfGeneratedId();
	}

	/**
	 * Call through to {@link DataType#generatedId()}
	 */
	public Object generatedId() {
		return dataType.generatedId();
	}

	/**
	 * Return the value of field in the data argument if it is not the default value for the class. If it is the default
	 * then null is returned.
	 */
	public <FV> FV getFieldValueIfNotDefault(Object object) throws SQLException {
		@SuppressWarnings("unchecked")
		FV fieldValue = (FV)extractJavaFieldValue(object);
		if (fieldValue == null) {
			return null;
		}
		boolean isDefault;
		if (field.getType() == boolean.class) {
			isDefault = fieldValue.equals(DEFAULT_VALUE_BOOLEAN);
		} else if (field.getType() == byte.class || field.getType() == Byte.class) {
			isDefault = fieldValue.equals(DEFAULT_VALUE_BYTE);
		} else if (field.getType() == char.class || field.getType() == Character.class) {
			isDefault = fieldValue.equals(DEFAULT_VALUE_CHAR);
		} else if (field.getType() == short.class || field.getType() == Short.class) {
			isDefault = fieldValue.equals(DEFAULT_VALUE_SHORT);
		} else if (field.getType() == int.class || field.getType() == Integer.class) {
			isDefault = fieldValue.equals(DEFAULT_VALUE_INT);
		} else if (field.getType() == long.class || field.getType() == Long.class) {
			isDefault = fieldValue.equals(DEFAULT_VALUE_LONG);
		} else if (field.getType() == float.class || field.getType() == Float.class) {
			isDefault = fieldValue.equals(DEFAULT_VALUE_FLOAT);
		} else if (field.getType() == double.class || field.getType() == Double.class) {
			isDefault = fieldValue.equals(DEFAULT_VALUE_DOUBLE);
		} else {
			isDefault = false;
		}
		if (isDefault) {
			return null;
		} else {
			return fieldValue;
		}
	}

	/**
	 * Return An instantiated {@link FieldType} or null if the field does not have a {@link DatabaseField} annotation.
	 * 
	 * @param recurseLevel
	 *            is used to make sure we done get in an infinite recursive loop if a foreign object refers to itself. =
	 */
	public static FieldType createFieldType(ConnectionSource connectionSource, String tableName, Field field,
			Class<?> parentClass, int recurseLevel) throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		DatabaseFieldConfig fieldConfig = DatabaseFieldConfig.fromField(databaseType, tableName, field);
		if (fieldConfig == null) {
			return null;
		} else {
			return new FieldType(connectionSource, tableName, field, fieldConfig, parentClass, recurseLevel);
		}
	}

	@Override
	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != this.getClass()) {
			return false;
		}
		FieldType other = (FieldType) arg;
		return field.equals(other.field);
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":name=" + field.getName() + ",class="
				+ field.getDeclaringClass().getSimpleName();
	}
}
