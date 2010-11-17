package com.j256.ormlite.field;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.misc.SqlExceptionUtil;
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

	private final String tableName;
	private final Field field;
	private final String fieldName;
	private final String dbColumnName;
	private final DataType dataType;
	private final Object defaultValue;
	private final int width;
	private final boolean canBeNull;
	private final boolean isId;
	private final boolean isGeneratedId;
	private final String generatedIdSequence;
	private final FieldConverter fieldConverter;
	private final TableInfo<?> foreignTableInfo;
	private final Method fieldGetMethod;
	private final Method fieldSetMethod;
	private final Map<String, Enum<?>> enumStringMap;
	private final Map<Integer, Enum<?>> enumValueMap;
	private final Enum<?> unknownEnumVal;
	private final boolean throwIfNull;
	private final String format;
	private final boolean unique;

	/**
	 * You should use {@link FieldType#createFieldType} to instantiate one of these field if you have a {@link Field}.
	 */
	public FieldType(DatabaseType databaseType, String tableName, Field field, DatabaseFieldConfig fieldConfig)
			throws SQLException {
		this.tableName = tableName;
		this.field = field;
		this.fieldName = field.getName();
		DataType dataType;
		if (fieldConfig.getDataType() == DataType.UNKNOWN) {
			dataType = DataType.lookupClass(field.getType());
		} else {
			dataType = fieldConfig.getDataType();
			if (!dataType.isValidForType(field.getType())) {
				throw new IllegalArgumentException("Field class " + field.getType() + " for field " + this
						+ " is not valid for data type " + dataType);
			}
		}
		String defaultFieldName = field.getName();
		if (fieldConfig.isForeign()) {
			if (dataType.isPrimitive()) {
				throw new IllegalArgumentException("Field " + this + " is a primitive class " + field.getType()
						+ " but marked as foreign");
			}
			DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();
			if (tableConfig == null) {
				tableConfig = DatabaseTableConfig.fromClass(databaseType, field.getType());
			}
			@SuppressWarnings("unchecked")
			TableInfo<?> foreignInfo = new TableInfo(databaseType, tableConfig);
			if (foreignInfo.getIdField() == null) {
				throw new IllegalArgumentException("Foreign field " + field.getType() + " does not have id field");
			}
			foreignTableInfo = foreignInfo;
			defaultFieldName = defaultFieldName + FOREIGN_ID_FIELD_SUFFIX;
			// this field's data type is the foreign id's type
			dataType = foreignInfo.getIdField().getDataType();
		} else if (dataType == DataType.UNKNOWN) {
			throw new IllegalArgumentException("ORMLite does not know how to store field class " + field.getType()
					+ " for field " + this);
		} else {
			foreignTableInfo = null;
		}
		if (fieldConfig.getColumnName() == null) {
			this.dbColumnName = databaseType.convertColumnName(defaultFieldName);
		} else {
			this.dbColumnName = databaseType.convertColumnName(fieldConfig.getColumnName());
		}
		this.dataType = dataType;
		this.width = fieldConfig.getWidth();
		this.canBeNull = fieldConfig.isCanBeNull();
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
		this.format = fieldConfig.getFormat();
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
		if (dataType == DataType.ENUM_INTEGER || dataType == DataType.ENUM_STRING) {
			this.enumStringMap = new HashMap<String, Enum<?>>();
			this.enumValueMap = new HashMap<Integer, Enum<?>>();
			Enum<?>[] constants = (Enum<?>[]) field.getType().getEnumConstants();
			if (constants == null) {
				throw new SQLException("Field " + field.getName() + " improperly configured as type " + dataType);
			}
			for (Enum<?> enumVal : constants) {
				this.enumStringMap.put(enumVal.name(), enumVal);
				this.enumValueMap.put(enumVal.ordinal(), enumVal);
			}
			this.unknownEnumVal = fieldConfig.getUnknownEnumvalue();
		} else {
			this.enumStringMap = null;
			this.enumValueMap = null;
			this.unknownEnumVal = null;
		}
		this.throwIfNull = fieldConfig.isThrowIfNull();
		if (this.throwIfNull && !dataType.isPrimitive()) {
			throw new SQLException("Field " + field.getName() + " must be a primitive if set with throwIfNull");
		}
		String defaultStr = fieldConfig.getDefaultValue();
		if (defaultStr == null || defaultStr.equals("")) {
			this.defaultValue = null;
		} else if (this.isGeneratedId) {
			throw new SQLException("Field '" + field.getName() + "' cannot be a generatedId and have a default value '"
					+ defaultStr + "'");
		} else {
			this.defaultValue = this.fieldConverter.parseDefaultString(this, defaultStr);
		}
		this.unique = fieldConfig.isUnique();
		if (this.isId && !dataType.isAppropriateId()) {
			throw new SQLException("Field '" + field.getName() + "' is of data type " + dataType
					+ " which cannot be the ID field");
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

	public SqlType getSqlTypeVal() {
		return fieldConverter.getSqlType();
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public int getWidth() {
		return width;
	}

	public boolean isCanBeNull() {
		return canBeNull;
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
	public TableInfo<?> getForeignTableInfo() {
		return foreignTableInfo;
	}

	/**
	 * Assign to the data object the val corresponding to the fieldType.
	 */
	public void assignField(Object data, Object val) throws SQLException {
		// if this is a foreign object then val is the foreign object's id val
		if (foreignTableInfo != null) {
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
			Object foreignObject = foreignTableInfo.createObject();
			// assign the val to its id field
			foreignTableInfo.getIdField().assignField(foreignObject, val);
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
			// may never get here because id fields _must_ be convert-able but let's be careful out there
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
	 * Return whether this field is a number.
	 */
	public boolean isEscapedValue() {
		return dataType.isEscapedValue();
	}

	/**
	 * Return the format of the field.
	 */
	public String getFormat() {
		return format;
	}

	public boolean isUnique() {
		return unique;
	}

	/**
	 * Return whether this field's default value should be escaped in SQL.
	 */
	public boolean isEscapeDefaultValue() {
		return dataType.isEscapeDefaultValue();
	}

	/**
	 * Return if this data type be compared in SQL statements.
	 */
	public boolean isComparable() {
		return dataType.isComparable();
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
		if (dataType.isPrimitive()) {
			if (throwIfNull && results.isNull(dbColumnPos)) {
				throw new SQLException("Results value for primitive field '" + fieldName
						+ "' was an invalid null value");
			}
		} else if (!fieldConverter.isStreamType() && results.isNull(dbColumnPos)) {
			// we can't check if we have a null if this is a stream type
			return null;
		}
		@SuppressWarnings("unchecked")
		T converted = (T) fieldConverter.resultToJava(this, results, dbColumnPos);
		return converted;
	}

	/**
	 * Get the Enum associated with the integer value.
	 */
	public Enum<?> enumFromInt(int val) throws SQLException {
		// just do this once
		Integer integerVal = new Integer(val);
		if (enumValueMap == null) {
			return enumVal(integerVal, null);
		} else {
			return enumVal(integerVal, enumValueMap.get(integerVal));
		}
	}

	/**
	 * Get the Enum associated with the String value.
	 */
	public Enum<?> enumFromString(String val) throws SQLException {
		if (enumStringMap == null) {
			return enumVal(val, null);
		} else {
			return enumVal(val, enumStringMap.get(val));
		}
	}

	/**
	 * Return An instantiated {@link FieldType} or null if the field does not have a {@link DatabaseField} annotation.
	 */
	public static FieldType createFieldType(DatabaseType databaseType, String tableName, Field field)
			throws SQLException {
		DatabaseFieldConfig fieldConfig = DatabaseFieldConfig.fromField(databaseType, field);
		if (fieldConfig == null) {
			return null;
		} else {
			return new FieldType(databaseType, tableName, field, fieldConfig);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":name=" + field.getName() + ",class="
				+ field.getDeclaringClass().getSimpleName();
	}

	private Enum<?> enumVal(Object val, Enum<?> enumVal) throws SQLException {
		if (enumVal != null) {
			return enumVal;
		} else if (unknownEnumVal == null) {
			throw new SQLException("Cannot get enum value of '" + val + "' for field " + field);
		} else {
			return unknownEnumVal;
		}
	}
}
