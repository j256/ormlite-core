package com.j256.ormlite.misc;

import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.JdbcType;

/**
 * Wanted to isolate the javax.persistence annotations in one class so it can easily be short-circuited.
 * 
 * @author graywatson
 */
public class JavaxPersistence {

	/**
	 * Create a field config from the javax.persistence annotations associated with the field argument. Returns null if
	 * none.
	 * 
	 * <p>
	 * <b> NOTE: </b> To remove the javax.persistence dependency, this method can just return null.
	 * </p>
	 */
	public static DatabaseFieldConfig createFieldConfig(DatabaseType databaseType, Field field) {
		Column column = field.getAnnotation(Column.class);
		Id id = field.getAnnotation(Id.class);
		GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
		OneToOne oneToOne = field.getAnnotation(OneToOne.class);
		ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
		if (column == null && id == null && oneToOne == null && manyToOne == null) {
			return null;
		}

		DatabaseFieldConfig config = new DatabaseFieldConfig();
		String fieldName = field.getName();
		if (databaseType.isEntityNamesMustBeUpCase()) {
			fieldName = fieldName.toUpperCase();
		}
		config.setFieldName(fieldName);
		if (column != null) {
			if (column.name().length() > 0) {
				config.setColumnName(column.name());
			}
			config.setWidth(column.length());
			config.setCanBeNull(column.nullable());
		}
		if (id != null) {
			if (generatedValue != null) {
				// generatedValue only works if it is also an id according to {@link GeneratedValue)
				config.setGeneratedId(true);
			} else {
				config.setId(true);
			}
		}
		// foreign values are always ones we can't map as primitives (or Strings)
		config.setForeign(oneToOne != null || manyToOne != null);
		config.setJdbcType(JdbcType.lookupClass(field.getType()));
		config.setUseGetSet(DatabaseFieldConfig.findGetMethod(field, false) != null
				&& DatabaseFieldConfig.findSetMethod(field, false) != null);
		return config;
	}

	/**
	 * Return the javax.persistence.Entity annotation name for the class argument or null if none or if there was no
	 * entity name.
	 * 
	 * <p>
	 * <b> NOTE: </b> To remove the javax.persistence dependency, this method can just return null.
	 * </p>
	 */
	public static String getEntityName(Class<?> clazz) {
		Entity entity = clazz.getAnnotation(Entity.class);
		if (entity != null && entity.name() != null && entity.name().length() > 0) {
			return entity.name();
		} else {
			return null;
		}
	}
}
