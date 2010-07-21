package com.j256.ormlite.field;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.misc.SqlExceptionUtil;
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

	private final Field field;
	private final String fieldName;
	private final String dbColumnName;
	private final JdbcType jdbcType;
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
	private final Map<String, Enum<?>> enumStringMap = new HashMap<String, Enum<?>>();
	private final Map<Integer, Enum<?>> enumValueMap = new HashMap<Integer, Enum<?>>();
	private final Enum<?> unknownEnumVal;
	private final boolean throwIfNull;

	/**
	 * You should use {@link FieldType#createFieldType} to instantiate one of these field if you have a {@link Field}.
	 */
	public FieldType(DatabaseType databaseType, String tableName, Field field, DatabaseFieldConfig fieldConfig)
			throws SQLException {
		this.field = field;
		this.fieldName = field.getName();
		JdbcType jdbcType;
		if (fieldConfig.getJdbcType() == JdbcType.UNKNOWN) {
			jdbcType = JdbcType.lookupClass(field.getType());
		} else {
			jdbcType = fieldConfig.getJdbcType();
			if (!jdbcType.isValidForType(field.getType())) {
				throw new IllegalArgumentException("Field class " + field.getType() + " for field " + this
						+ " is not valid for jdbc type " + jdbcType);
			}
		}
		String defaultFieldName = field.getName();
		if (jdbcType == JdbcType.UNKNOWN) {
			if (!fieldConfig.isForeign()) {
				throw new IllegalArgumentException("ORMLite does not know how to store field class " + field.getType()
						+ " for non-foreign field " + this);
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
			// this field's jdbcType is the foreign id's jdbctype
			jdbcType = foreignInfo.getIdField().getJdbcType();
		} else {
			if (fieldConfig.isForeign()) {
				throw new IllegalArgumentException("Field " + this + " is a primitive class " + field.getType()
						+ " but marked as foreign");
			}
			foreignTableInfo = null;
		}
		if (fieldConfig.getColumnName() == null) {
			this.dbColumnName = databaseType.convertColumnName(defaultFieldName);
		} else {
			this.dbColumnName = databaseType.convertColumnName(fieldConfig.getColumnName());
		}
		this.jdbcType = jdbcType;
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
				String seqName = databaseType.generateIdSequenceName(tableName, this);
				if (databaseType.isEntityNamesMustBeUpCase()) {
					seqName = seqName.toUpperCase();
				}
				this.generatedIdSequence = seqName;
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
		if (this.isGeneratedId && !this.jdbcType.isValidGeneratedType()) {
			throw new IllegalArgumentException("Generated field " + field.getName()
					+ " is not an appropriate type in class " + field.getDeclaringClass());
		}
		FieldConverter converter = databaseType.getFieldConverter(this);
		if (converter == null) {
			this.fieldConverter = jdbcType;
		} else {
			this.fieldConverter = converter;
		}
		String defaultStr = fieldConfig.getDefaultValue();
		if (defaultStr == null || defaultStr.equals("")) {
			this.defaultValue = null;
		} else if (this.isGeneratedId) {
			throw new SQLException("Field '" + field.getName() + "' cannot be a generatedId and have a default value '"
					+ defaultStr + "'");
		} else {
			this.defaultValue = this.fieldConverter.parseDefaultString(defaultStr);
		}
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
		if (jdbcType == JdbcType.ENUM_INTEGER || jdbcType == JdbcType.ENUM_STRING) {
			for (Enum<?> enumVal : (Enum<?>[]) field.getType().getEnumConstants()) {
				enumStringMap.put(enumVal.name(), enumVal);
				enumValueMap.put(enumVal.ordinal(), enumVal);
			}
			this.unknownEnumVal = fieldConfig.getUnknownEnumvalue();
		} else {
			this.unknownEnumVal = null;
		}
		this.throwIfNull = fieldConfig.isThrowIfNull();
		if (this.throwIfNull && !jdbcType.isPrimitive()) {
			throw new SQLException("Field " + field.getName()
					+ " must be a primitive if set with throwIfNull");
		}
	}

	/**
	 * Return the column name either specified my {@link DatabaseField#columnName} or from {@link Field#getName}.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Return the column name either specified my {@link DatabaseField#columnName} or from {@link Field#getName}.
	 */
	public String getDbColumnName() {
		return dbColumnName;
	}

	/**
	 * Return the {@link JdbcType} associated with this.
	 */
	public JdbcType getJdbcType() {
		return jdbcType;
	}

	/**
	 * Return the JDBC type value.
	 */
	public int getJdbcTypeVal() {
		return fieldConverter.getJdbcTypeVal();
	}

	/**
	 * Return the default value configured by {@link DatabaseField#defaultValue} or null if none.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Return the width of the field as configured by {@link DatabaseField#width}.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Return whether the field can be assigned to null as configured by {@link DatabaseField#canBeNull}.
	 */
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

	/**
	 * Return whether or not the field is a foreign object field.
	 */
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
	 * Convert the default value string into a valid Java object for this type.
	 */
	public Object parseDefaultString(String defaulStr) throws SQLException {
		return fieldConverter.parseDefaultString(defaulStr);
	}

	/**
	 * Assign to the data object the val corresponding to the fieldType.
	 */
	public void assignField(Object data, Object val) throws SQLException {
		// if this is a foreign object then val is the foreign object's id val
		if (foreignTableInfo != null) {
			Object foreignId = getConvertedFieldValue(data);
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
		Object idVal = jdbcType.convertIdNumber(val);
		if (idVal == null) {
			// may never get here because id fields _must_ be convert-able but let's be careful out there
			throw new SQLException("Invalid class " + jdbcType + " for sequence-id " + this);
		} else {
			assignField(data, idVal);
			return idVal;
		}
	}

	/**
	 * Return the value from the field in the object that is defined by this FieldType.
	 */
	public <FV> FV getFieldValue(Object object) throws SQLException {
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
			val = foreignTableInfo.getIdField().getConvertedFieldValue(val);
		}

		@SuppressWarnings("unchecked")
		FV converted = (FV) val;
		return converted;
	}

	/**
	 * Return the value from the field in the object after it has been converted to something suitable to be stored in
	 * the database.
	 */
	public <FV> FV getConvertedFieldValue(Object object) throws SQLException {
		Object val = getFieldValue(object);
		if (val == null) {
			return null;
		} else {
			val = fieldConverter.javaToArg(val);
			@SuppressWarnings("unchecked")
			FV converted = (FV) val;
			return converted;
		}
	}

	public boolean isThrowIfNull() {
		return throwIfNull;
	}

	/**
	 * Return whether this field is a number.
	 */
	public boolean isNumber() {
		return jdbcType.isNumber();
	}

	/**
	 * Return whether this field's default value should be escaped in SQL.
	 */
	public boolean escapeDefaultValue() {
		return jdbcType.escapeDefaultValue();
	}

	/**
	 * Get the result object from the {@link ResultSet}. A call through to {@link FieldConverter#resultToJava}.
	 */
	public <T> T resultToJava(ResultSet resultSet, Map<String, Integer> columnPositions) throws SQLException {
		Integer dbColumnPos = columnPositions.get(dbColumnName);
		if (dbColumnPos == null) {
			dbColumnPos = resultSet.findColumn(dbColumnName);
			columnPositions.put(dbColumnName, dbColumnPos);
		}
		if (jdbcType.isPrimitive()) {
			if (throwIfNull && resultSet.getObject(dbColumnPos) == null) {
				throw new SQLException("ResultSet value for primitive field '" + fieldName
						+ "' was an invalid null value");
			}
		} else if (!fieldConverter.isStreamType() && resultSet.getObject(dbColumnPos) == null) {
			// we can't check if we have a null if this is a stream type
			return null;
		}
		@SuppressWarnings("unchecked")
		T converted = (T) fieldConverter.resultToJava(this, resultSet, dbColumnPos);
		return converted;
	}

	/**
	 * Get the Enum associated with the integer value.
	 * 
	 * NOTE: It is an Integer object for a reason. No changie.
	 */
	public Enum<?> enumFromInt(Integer val) throws SQLException {
		return enumVal(val, enumValueMap.get(val));
	}

	/**
	 * Get the Enum associated with the String value.
	 */
	public Enum<?> enumFromString(String val) throws SQLException {
		return enumVal(val, enumStringMap.get(val));
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
		return getClass().getSimpleName() + ":name=" + field.getName() + ",class=" + field.getDeclaringClass();
	}

	private Enum<?> enumVal(Object val, Enum<?> enumVal) throws SQLException {
		if (enumVal == null) {
			if (unknownEnumVal == null) {
				throw new SQLException("Cannot get enum value of '" + val + "' for field " + field);
			} else {
				return unknownEnumVal;
			}
		} else {
			return enumVal;
		}
	}
}
