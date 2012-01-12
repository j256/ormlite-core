package com.j256.ormlite.field;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.types.VoidType;
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

	/*
	 * Default values.
	 * 
	 * NOTE: These don't get any values so the compiler assigns them to the default values for the type. Ahhhh. Smart.
	 */
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
	private final String columnName;
	private final DatabaseFieldConfig fieldConfig;
	private final boolean isId;
	private final boolean isGeneratedId;
	private final String generatedIdSequence;
	private final Method fieldGetMethod;
	private final Method fieldSetMethod;

	private DataPersister dataPersister;
	private Object defaultValue;
	private Object dataTypeConfigObj;

	private FieldConverter fieldConverter;
	private FieldType foreignIdField;
	private Constructor<?> foreignConstructor;
	private FieldType foreignFieldType;
	private Dao<?, ?> foreignDao;
	private MappedQueryForId<Object, Object> mappedQueryForId;

	private static final ThreadLocal<LevelCounters> threadLevelCounters = new ThreadLocal<LevelCounters>();

	/**
	 * You should use {@link FieldType#createFieldType} to instantiate one of these field if you have a {@link Field}.
	 */
	public FieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig,
			Class<?> parentClass) throws SQLException {
		this.connectionSource = connectionSource;
		this.tableName = tableName;
		DatabaseType databaseType = connectionSource.getDatabaseType();
		this.field = field;
		Class<?> clazz = field.getType();
		DataPersister dataPersister;
		if (fieldConfig.getDataPersister() == null) {
			Class<? extends DataPersister> persisterClass = fieldConfig.getPersisterClass();
			if (persisterClass == null || persisterClass == VoidType.class) {
				dataPersister = DataPersisterManager.lookupForField(field);
			} else {
				Method method;
				try {
					method = persisterClass.getDeclaredMethod("getSingleton");
				} catch (Exception e) {
					throw SqlExceptionUtil.create("Could not find getSingleton static method on class "
							+ persisterClass, e);
				}
				Object result;
				try {
					result = method.invoke(null);
				} catch (InvocationTargetException e) {
					throw SqlExceptionUtil.create("Could not run getSingleton method on class " + persisterClass,
							e.getTargetException());
				} catch (Exception e) {
					throw SqlExceptionUtil.create("Could not run getSingleton method on class " + persisterClass, e);
				}
				if (result == null) {
					throw new SQLException("Static getSingleton method should not return null on class "
							+ persisterClass);
				}
				try {
					dataPersister = (DataPersister) result;
				} catch (Exception e) {
					throw SqlExceptionUtil.create(
							"Could not cast result of static getSingleton method to DataPersister from class "
									+ persisterClass, e);
				}
			}
		} else {
			dataPersister = fieldConfig.getDataPersister();
			if (!dataPersister.isValidForField(field)) {
				throw new IllegalArgumentException("Field class " + clazz + " for field " + this
						+ " is not valid for data persister " + dataPersister);
			}
		}
		String defaultFieldName = field.getName();
		if (fieldConfig.isForeign() || fieldConfig.isForeignAutoRefresh()) {
			if (dataPersister != null && dataPersister.isPrimitive()) {
				throw new IllegalArgumentException("Field " + this + " is a primitive class " + clazz
						+ " but marked as foreign");
			}
			defaultFieldName = defaultFieldName + FOREIGN_ID_FIELD_SUFFIX;
			if (ForeignCollection.class.isAssignableFrom(clazz)) {
				throw new SQLException("Field '" + field.getName() + "' in class " + clazz + "' should use the @"
						+ ForeignCollectionField.class.getSimpleName() + " annotation not foreign=true");
			}
		} else if (fieldConfig.isForeignCollection()) {
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
		} else if (dataPersister == null && (!fieldConfig.isForeignCollection())) {
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
		}
		if (fieldConfig.getColumnName() == null) {
			this.columnName = defaultFieldName;
		} else {
			this.columnName = fieldConfig.getColumnName();
		}
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
		if (this.isId && (fieldConfig.isForeign() || fieldConfig.isForeignAutoRefresh())) {
			throw new IllegalArgumentException("Id field " + field.getName() + " cannot also be a foreign object");
		}
		if (fieldConfig.isUseGetSet()) {
			this.fieldGetMethod = DatabaseFieldConfig.findGetMethod(field, true);
			this.fieldSetMethod = DatabaseFieldConfig.findSetMethod(field, true);
		} else {
			if (!field.isAccessible()) {
				try {
					this.field.setAccessible(true);
				} catch (SecurityException e) {
					throw new IllegalArgumentException("Could not open access to field " + field.getName()
							+ ".  You may have to set useGetSet=true to fix.");
				}
			}
			this.fieldGetMethod = null;
			this.fieldSetMethod = null;
		}
		if (fieldConfig.isAllowGeneratedIdInsert() && !fieldConfig.isGeneratedId()) {
			throw new IllegalArgumentException("Field " + field.getName()
					+ " must be a generated-id if allowGeneratedIdInsert = true");
		}
		if (fieldConfig.isForeignAutoRefresh() && !fieldConfig.isForeign()) {
			throw new IllegalArgumentException("Field " + field.getName()
					+ " must have foreign = true if foreignAutoRefresh = true");
		}
		if (fieldConfig.isForeignAutoCreate() && !fieldConfig.isForeign()) {
			throw new IllegalArgumentException("Field " + field.getName()
					+ " must have foreign = true if foreignAutoCreate = true");
		}
		if (fieldConfig.isVersion() && (dataPersister == null || !dataPersister.isValidForVersion())) {
			throw new IllegalArgumentException("Field " + field.getName()
					+ " is not a valid type to be a version field");
		}
		assignDataType(databaseType, dataPersister);
	}

	/**
	 * Because we go recursive in a lot of situations if we construct DAOs inside of the FieldType constructor, we have
	 * to do this 2nd pass initialization so we can better use the DAO caches.
	 * 
	 * @see BaseDaoImpl#initialize()
	 */
	public void configDaoInformation(ConnectionSource connectionSource, Class<?> parentClass) throws SQLException {
		Class<?> clazz = field.getType();
		DatabaseType databaseType = connectionSource.getDatabaseType();
		TableInfo<?, ?> foreignTableInfo;
		final FieldType foreignIdField;
		final Constructor<?> foreignConstructor;
		final FieldType foreignFieldType;
		final Dao<?, ?> foreignDao;
		final MappedQueryForId<Object, Object> mappedQueryForId;

		if (fieldConfig.isForeignAutoRefresh()) {
			DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();
			if (tableConfig == null) {
				// NOTE: the cast is necessary for maven
				foreignDao = (BaseDaoImpl<?, ?>) DaoManager.createDao(connectionSource, clazz);
				foreignTableInfo = ((BaseDaoImpl<?, ?>) foreignDao).getTableInfo();
			} else {
				tableConfig.extractFieldTypes(connectionSource);
				// NOTE: the cast is necessary for maven
				foreignDao = (BaseDaoImpl<?, ?>) DaoManager.createDao(connectionSource, tableConfig);
				foreignTableInfo = ((BaseDaoImpl<?, ?>) foreignDao).getTableInfo();
			}
			foreignIdField = foreignTableInfo.getIdField();
			if (foreignIdField == null) {
				throw new IllegalArgumentException("Foreign field " + clazz + " does not have id field");
			}
			@SuppressWarnings("unchecked")
			MappedQueryForId<Object, Object> castMappedQueryForId =
					(MappedQueryForId<Object, Object>) MappedQueryForId.build(databaseType, foreignTableInfo);
			mappedQueryForId = castMappedQueryForId;
			foreignConstructor = foreignTableInfo.getConstructor();
			foreignFieldType = null;
		} else if (fieldConfig.isForeign()) {
			if (this.dataPersister != null && this.dataPersister.isPrimitive()) {
				throw new IllegalArgumentException("Field " + this + " is a primitive class " + clazz
						+ " but marked as foreign");
			}
			DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();
			if (tableConfig != null) {
				tableConfig.extractFieldTypes(connectionSource);
				// NOTE: the cast is necessary for maven
				foreignDao = (BaseDaoImpl<?, ?>) DaoManager.createDao(connectionSource, tableConfig);
				foreignTableInfo = ((BaseDaoImpl<?, ?>) foreignDao).getTableInfo();
				foreignIdField = foreignTableInfo.getIdField();
				foreignConstructor = foreignTableInfo.getConstructor();
			} else if (BaseDaoEnabled.class.isAssignableFrom(clazz) || fieldConfig.isForeignAutoCreate()) {
				// NOTE: the cast is necessary for maven
				foreignDao = (BaseDaoImpl<?, ?>) DaoManager.createDao(connectionSource, clazz);
				foreignTableInfo = ((BaseDaoImpl<?, ?>) foreignDao).getTableInfo();
				foreignIdField = foreignTableInfo.getIdField();
				foreignConstructor = foreignTableInfo.getConstructor();
			} else {
				foreignDao = null;
				foreignIdField =
						DatabaseTableConfig.extractIdFieldType(connectionSource, clazz,
								DatabaseTableConfig.extractTableName(clazz));
				foreignConstructor = DatabaseTableConfig.findNoArgConstructor(clazz);
			}
			if (foreignIdField == null) {
				throw new IllegalArgumentException("Foreign field " + clazz + " does not have id field");
			}
			if (isForeignAutoCreate() && !foreignIdField.isGeneratedId()) {
				throw new IllegalArgumentException("Field " + field.getName()
						+ ", if foreignAutoCreate = true then class " + clazz.getSimpleName()
						+ " must have id field with generatedId = true");
			}
			foreignFieldType = null;
			mappedQueryForId = null;
		} else if (fieldConfig.isForeignCollection()) {
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
			Dao<Object, Object> foundDao;
			if (tableConfig == null) {
				@SuppressWarnings("unchecked")
				Dao<Object, Object> castDao = (Dao<Object, Object>) DaoManager.createDao(connectionSource, clazz);
				foundDao = castDao;
			} else {
				@SuppressWarnings("unchecked")
				Dao<Object, Object> castDao = (Dao<Object, Object>) DaoManager.createDao(connectionSource, tableConfig);
				foundDao = castDao;
			}
			FieldType findForeignFieldType = null;
			String foreignColumn = fieldConfig.getForeignCollectionColumn();
			for (FieldType fieldType : ((BaseDaoImpl<?, ?>) foundDao).getTableInfo().getFieldTypes()) {
				if (fieldType.getType() == parentClass
						&& (foreignColumn == null || fieldType.getField().getName().equals(foreignColumn))) {
					findForeignFieldType = fieldType;
					break;
				}
			}
			if (findForeignFieldType == null) {
				throw new SQLException("Foreign collection object " + clazz + " for field '" + field.getName()
						+ "' column-name does not contain a foreign field "
						+ (foreignColumn == null ? "" : " named '" + foreignColumn + "'") + " of class " + parentClass);
			}
			if (!findForeignFieldType.fieldConfig.isForeign()
					&& !findForeignFieldType.fieldConfig.isForeignAutoRefresh()) {
				// this may never be reached
				throw new SQLException("Foreign collection object " + clazz + " for field '" + field.getName()
						+ "' contains a field of class " + parentClass + " but it's not foreign");
			}
			foreignDao = foundDao;
			foreignFieldType = findForeignFieldType;
			foreignIdField = null;
			foreignConstructor = null;
			mappedQueryForId = null;
		} else {
			foreignConstructor = null;
			foreignIdField = null;
			foreignFieldType = null;
			foreignDao = null;
			mappedQueryForId = null;
		}

		this.mappedQueryForId = mappedQueryForId;
		this.foreignConstructor = foreignConstructor;
		this.foreignFieldType = foreignFieldType;
		this.foreignDao = foreignDao;
		this.foreignIdField = foreignIdField;

		if (foreignIdField != null) {
			assignDataType(databaseType, foreignIdField.getDataPersister());
		}
	}

	public Field getField() {
		return field;
	}

	public String getFieldName() {
		return field.getName();
	}

	/**
	 * Return the class of the field associated with this field type.
	 */
	public Class<?> getType() {
		return field.getType();
	}

	public String getColumnName() {
		return columnName;
	}

	public DataPersister getDataPersister() {
		return dataPersister;
	}

	public Object getDataTypeConfigObj() {
		return dataTypeConfigObj;
	}

	public SqlType getSqlType() {
		return fieldConverter.getSqlType();
	}

	/**
	 * Return the default value as parsed from the {@link DatabaseFieldConfig#getDefaultValue()}.
	 */
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
		return fieldConfig.isForeign();
	}

	/**
	 * Assign to the data object the val corresponding to the fieldType.
	 */
	public void assignField(Object data, Object val, boolean parentObject, ObjectCache objectCache) throws SQLException {
		// if this is a foreign object then val is the foreign object's id val
		if (foreignIdField != null && val != null) {
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
			if (!parentObject) {
				Object foreignObject;
				/*
				 * If we don't have a mappedQueryForId or if we have recursed the proper number of times, just return a
				 * shell with the id set.
				 */
				LevelCounters levelCounters = getLevelCounters();
				if (mappedQueryForId == null
						|| levelCounters.autoRefreshlevel >= fieldConfig.getMaxForeignAutoRefreshLevel()) {
					// create a shell and assign its id field
					foreignObject = TableInfo.createObject(foreignConstructor, foreignDao);
					foreignIdField.assignField(foreignObject, val, false, objectCache);
				} else {
					levelCounters.autoRefreshlevel++;
					try {
						DatabaseConnection databaseConnection = connectionSource.getReadOnlyConnection();
						try {
							foreignObject = mappedQueryForId.execute(databaseConnection, val, objectCache);
						} finally {
							connectionSource.releaseConnection(databaseConnection);
						}
					} finally {
						levelCounters.autoRefreshlevel--;
					}
				}
				// the value we are to assign to our field is now the foreign object itself
				val = foreignObject;
			}
		}

		if (fieldSetMethod == null) {
			try {
				field.set(data, val);
			} catch (IllegalArgumentException e) {
				throw SqlExceptionUtil.create("Could not assign object '" + val + "' to field " + this, e);
			} catch (IllegalAccessException e) {
				throw SqlExceptionUtil.create("Could not assign object '" + val + "' to field " + this, e);
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
	public Object assignIdValue(Object data, Number val, ObjectCache objectCache) throws SQLException {
		Object idVal = dataPersister.convertIdNumber(val);
		if (idVal == null) {
			throw new SQLException("Invalid class " + dataPersister + " for sequence-id " + this);
		} else {
			assignField(data, idVal, false, objectCache);
			return idVal;
		}
	}

	/**
	 * Return the value from the field in the object that is defined by this FieldType.
	 */
	public <FV> FV extractRawJavaFieldValue(Object object) throws SQLException {
		Object val;
		if (fieldGetMethod == null) {
			try {
				// field object may not be a T yet
				val = field.get(object);
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not get field value for " + this, e);
			}
		} else {
			try {
				val = fieldGetMethod.invoke(object);
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not call " + fieldGetMethod + " for " + this, e);
			}
		}

		@SuppressWarnings("unchecked")
		FV converted = (FV) val;
		return converted;
	}

	/**
	 * Return the value from the field in the object that is defined by this FieldType. If the field is a foreign object
	 * then the ID of the field is returned instead.
	 */
	public Object extractJavaFieldValue(Object object) throws SQLException {

		Object val = extractRawJavaFieldValue(object);

		// if this is a foreign object then we want its id field
		if (foreignIdField != null && val != null) {
			val = foreignIdField.extractRawJavaFieldValue(val);
		}

		return val;
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
	public Object convertJavaFieldToSqlArgValue(Object fieldVal) throws SQLException {
		if (fieldVal == null) {
			return null;
		} else {
			return fieldConverter.javaToSqlArg(this, fieldVal);
		}
	}

	/**
	 * Move the SQL value to the next one for version processing.
	 */
	public Object moveToNextValue(Object val) {
		if (dataPersister == null) {
			return null;
		} else {
			return dataPersister.moveToNextValue(val);
		}
	}

	/**
	 * Return the id field associated with the foreign object or null if none.
	 */
	public FieldType getForeignIdField() {
		return foreignIdField;
	}

	/**
	 * Call through to {@link DataPersister#isEscapedValue()}
	 */
	public boolean isEscapedValue() {
		return dataPersister.isEscapedValue();
	}

	public Enum<?> getUnknownEnumVal() {
		return fieldConfig.getUnknownEnumValue();
	}

	/**
	 * Return the format of the field.
	 */
	public String getFormat() {
		return fieldConfig.getFormat();
	}

	public boolean isUnique() {
		return fieldConfig.isUnique();
	}

	public boolean isUniqueCombo() {
		return fieldConfig.isUniqueCombo();
	}

	public String getIndexName() {
		return fieldConfig.getIndexName(tableName);
	}

	public String getUniqueIndexName() {
		return fieldConfig.getUniqueIndexName(tableName);
	}

	/**
	 * Call through to {@link DataPersister#isEscapedDefaultValue()}
	 */
	public boolean isEscapedDefaultValue() {
		return dataPersister.isEscapedDefaultValue();
	}

	/**
	 * Call through to {@link DataPersister#isComparable()}
	 */
	public boolean isComparable() {
		return dataPersister.isComparable();
	}

	/**
	 * Call through to {@link DataPersister#isArgumentHolderRequired()}
	 */
	public boolean isArgumentHolderRequired() {
		return dataPersister.isArgumentHolderRequired();
	}

	/**
	 * Call through to {@link DatabaseFieldConfig#isForeignCollection()}
	 */
	public boolean isForeignCollection() {
		return fieldConfig.isForeignCollection();
	}

	/**
	 * Build and return a foreign collection based on the field settings that matches the id argument. This can return
	 * null in certain circumstances.
	 * 
	 * @param parent
	 *            The parent object that we will set on each item in the collection.
	 * @param id
	 *            The id of the foreign object we will look for. This can be null if we are creating an empty
	 *            collection.
	 * @param forceEager
	 *            Set to true to force this to be an eager collection.
	 */
	public <FT, FID> BaseForeignCollection<FT, FID> buildForeignCollection(FT parent, FID id, boolean forceEager)
			throws SQLException {
		// this can happen if we have a foreign-auto-refresh scenario
		if (foreignFieldType == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Dao<FT, FID> castDao = (Dao<FT, FID>) foreignDao;
		if (!fieldConfig.isForeignCollectionEager() && !forceEager) {
			// we know this won't go recursive so no need for the counters
			return new LazyForeignCollection<FT, FID>(castDao, parent, id, foreignFieldType.columnName,
					fieldConfig.getForeignCollectionOrderColumn());
		}

		LevelCounters levelCounters = getLevelCounters();
		// are we over our level limit?
		if (levelCounters.foreignCollectionLevel >= fieldConfig.getMaxEagerForeignCollectionLevel()) {
			// then return a lazy collection instead
			return new LazyForeignCollection<FT, FID>(castDao, parent, id, foreignFieldType.columnName,
					fieldConfig.getForeignCollectionOrderColumn());
		}
		levelCounters.foreignCollectionLevel++;
		try {
			return new EagerForeignCollection<FT, FID>(castDao, parent, id, foreignFieldType.columnName,
					fieldConfig.getForeignCollectionOrderColumn());
		} finally {
			levelCounters.foreignCollectionLevel--;
		}
	}

	/**
	 * Get the result object from the results. A call through to {@link FieldConverter#resultToJava}.
	 */
	public <T> T resultToJava(DatabaseResults results, Map<String, Integer> columnPositions) throws SQLException {
		Integer dbColumnPos = columnPositions.get(columnName);
		if (dbColumnPos == null) {
			dbColumnPos = results.findColumn(columnName);
			columnPositions.put(columnName, dbColumnPos);
		}
		@SuppressWarnings("unchecked")
		T converted = (T) fieldConverter.resultToJava(this, results, dbColumnPos);
		if (fieldConfig.isForeign()) {
			/*
			 * Subtle problem here. If your foreign field is a primitive and the value was null then this would return 0
			 * from getInt(). We have to specifically test to see if we have a foreign field so if it is null we return
			 * a null value to not create the sub-object.
			 */
			if (results.wasNull(dbColumnPos)) {
				return null;
			}
		} else if (dataPersister.isPrimitive()) {
			if (fieldConfig.isThrowIfNull() && results.wasNull(dbColumnPos)) {
				throw new SQLException("Results value for primitive field '" + field.getName()
						+ "' was an invalid null value");
			}
		} else if (!fieldConverter.isStreamType() && results.wasNull(dbColumnPos)) {
			// we can't check if we have a null if this is a stream type
			return null;
		}
		return converted;
	}

	/**
	 * Call through to {@link DataPersister#isSelfGeneratedId()}
	 */
	public boolean isSelfGeneratedId() {
		return dataPersister.isSelfGeneratedId();
	}

	/**
	 * Call through to {@link DatabaseFieldConfig#isAllowGeneratedIdInsert()}
	 */
	public boolean isAllowGeneratedIdInsert() {
		return fieldConfig.isAllowGeneratedIdInsert();
	}

	/**
	 * Call through to {@link DatabaseFieldConfig#getColumnDefinition()}
	 */
	public String getColumnDefinition() {
		return fieldConfig.getColumnDefinition();
	}

	/**
	 * Call through to {@link DatabaseFieldConfig#isForeignAutoCreate()}
	 */
	public boolean isForeignAutoCreate() {
		return fieldConfig.isForeignAutoCreate();
	}

	/**
	 * Call through to {@link DatabaseFieldConfig#isVersion()}
	 */
	public boolean isVersion() {
		return fieldConfig.isVersion();
	}

	/**
	 * Call through to {@link DataPersister#generateId()}
	 */
	public Object generateId() {
		return dataPersister.generateId();
	}

	/**
	 * Return the value of field in the data argument if it is not the default value for the class. If it is the default
	 * then null is returned.
	 */
	public <FV> FV getFieldValueIfNotDefault(Object object) throws SQLException {
		@SuppressWarnings("unchecked")
		FV fieldValue = (FV) extractJavaFieldValue(object);
		if (isFieldValueDefault(fieldValue)) {
			return null;
		} else {
			return fieldValue;
		}
	}

	/**
	 * Return whether or not the data object has a default value passed for this field of this type.
	 */
	public boolean isObjectsFieldValueDefault(Object object) throws SQLException {
		Object fieldValue = extractJavaFieldValue(object);
		return isFieldValueDefault(fieldValue);
	}

	/**
	 * Return whether or not the field value passed in is the default value for the type of the field. Null will return
	 * true.
	 */
	public Object getJavaDefaultValueDefault() {
		if (field.getType() == boolean.class) {
			return DEFAULT_VALUE_BOOLEAN;
		} else if (field.getType() == byte.class || field.getType() == Byte.class) {
			return DEFAULT_VALUE_BYTE;
		} else if (field.getType() == char.class || field.getType() == Character.class) {
			return DEFAULT_VALUE_CHAR;
		} else if (field.getType() == short.class || field.getType() == Short.class) {
			return DEFAULT_VALUE_SHORT;
		} else if (field.getType() == int.class || field.getType() == Integer.class) {
			return DEFAULT_VALUE_INT;
		} else if (field.getType() == long.class || field.getType() == Long.class) {
			return DEFAULT_VALUE_LONG;
		} else if (field.getType() == float.class || field.getType() == Float.class) {
			return DEFAULT_VALUE_FLOAT;
		} else if (field.getType() == double.class || field.getType() == Double.class) {
			return DEFAULT_VALUE_DOUBLE;
		} else {
			return null;
		}
	}

	/**
	 * Return whether or not the field value passed in is the default value for the type of the field. Null will return
	 * true.
	 */
	private boolean isFieldValueDefault(Object fieldValue) {
		if (fieldValue == null) {
			return true;
		} else {
			return fieldValue.equals(getJavaDefaultValueDefault());
		}
	}

	/**
	 * Pass the foreign data argument to the foreign {@link Dao#create(Object)} method.
	 */
	public <T> int createWithForeignDao(T foreignData) throws SQLException {
		@SuppressWarnings("unchecked")
		Dao<T, ?> castDao = (Dao<T, ?>) foreignDao;
		return castDao.create(foreignData);
	}

	/**
	 * Return An instantiated {@link FieldType} or null if the field does not have a {@link DatabaseField} annotation.
	 */
	public static FieldType createFieldType(ConnectionSource connectionSource, String tableName, Field field,
			Class<?> parentClass) throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		DatabaseFieldConfig fieldConfig = DatabaseFieldConfig.fromField(databaseType, tableName, field);
		if (fieldConfig == null) {
			return null;
		} else {
			return new FieldType(connectionSource, tableName, field, fieldConfig, parentClass);
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

	/**
	 * Configure our data persister and any dependent fields. We have to do this here because both the constructor and
	 * {@link #configDaoInformation} method can set the data-type.
	 */
	private void assignDataType(DatabaseType databaseType, DataPersister dataPersister) throws SQLException {
		this.dataPersister = dataPersister;
		if (dataPersister == null) {
			return;
		}
		this.fieldConverter = databaseType.getFieldConverter(dataPersister);
		if (this.isGeneratedId && !dataPersister.isValidGeneratedType()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Generated-id field '").append(field.getName());
			sb.append("' in ").append(field.getDeclaringClass().getSimpleName());
			sb.append(" can't be type ").append(this.dataPersister.getSqlType());
			sb.append(".  Must be one of: ");
			for (DataType dataType : DataType.values()) {
				DataPersister persister = dataType.getDataPersister();
				if (persister != null && persister.isValidGeneratedType()) {
					sb.append(dataType).append(' ');
				}
			}
			throw new IllegalArgumentException(sb.toString());
		}
		if (fieldConfig.isThrowIfNull() && !dataPersister.isPrimitive()) {
			throw new SQLException("Field " + field.getName() + " must be a primitive if set with throwIfNull");
		}
		if (this.isId && !dataPersister.isAppropriateId()) {
			throw new SQLException("Field '" + field.getName() + "' is of data type " + dataPersister
					+ " which cannot be the ID field");
		}
		this.dataTypeConfigObj = dataPersister.makeConfigObject(this);
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

	private LevelCounters getLevelCounters() {
		LevelCounters levelCounters = threadLevelCounters.get();
		if (levelCounters == null) {
			levelCounters = new LevelCounters();
			threadLevelCounters.set(levelCounters);
		}
		return levelCounters;
	}

	private static class LevelCounters {
		int autoRefreshlevel;
		int foreignCollectionLevel;
	}
}
