package com.j256.ormlite.misc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collection;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;

/**
 * Class for isolating the detection of the javax.persistence annotations. This used to be a hard dependency but it has
 * become optinal/test since we use reflection here.
 * 
 * @author graywatson
 */
public class JavaxPersistence {

	/**
	 * Create a field config from the javax.persistence annotations associated with the field argument. Returns null if
	 * none.
	 */
	public static DatabaseFieldConfig createFieldConfig(DatabaseType databaseType, Field field) throws SQLException {
		Annotation columnAnnotation = null;
		Annotation basicAnnotation = null;
		Annotation idAnnotation = null;
		Annotation generatedValueAnnotation = null;
		Annotation oneToOneAnnotation = null;
		Annotation manyToOneAnnotation = null;
		Annotation joinColumnAnnotation = null;
		Annotation enumeratedAnnotation = null;
		Annotation versionAnnotation = null;

		for (Annotation annotation : field.getAnnotations()) {
			Class<?> annotationClass = annotation.annotationType();
			if (annotationClass.getName().equals("javax.persistence.Column")) {
				columnAnnotation = annotation;
			}
			if (annotationClass.getName().equals("javax.persistence.Basic")) {
				basicAnnotation = annotation;
			}
			if (annotationClass.getName().equals("javax.persistence.Id")) {
				idAnnotation = annotation;
			}
			if (annotationClass.getName().equals("javax.persistence.GeneratedValue")) {
				generatedValueAnnotation = annotation;
			}
			if (annotationClass.getName().equals("javax.persistence.OneToOne")) {
				oneToOneAnnotation = annotation;
			}
			if (annotationClass.getName().equals("javax.persistence.ManyToOne")) {
				manyToOneAnnotation = annotation;
			}
			if (annotationClass.getName().equals("javax.persistence.JoinColumn")) {
				joinColumnAnnotation = annotation;
			}
			if (annotationClass.getName().equals("javax.persistence.Enumerated")) {
				enumeratedAnnotation = annotation;
			}
			if (annotationClass.getName().equals("javax.persistence.Version")) {
				versionAnnotation = annotation;
			}
		}

		if (columnAnnotation == null && basicAnnotation == null && idAnnotation == null && oneToOneAnnotation == null
				&& manyToOneAnnotation == null && enumeratedAnnotation == null && versionAnnotation == null) {
			return null;
		}

		DatabaseFieldConfig config = new DatabaseFieldConfig();
		String fieldName = field.getName();
		if (databaseType.isEntityNamesMustBeUpCase()) {
			fieldName = fieldName.toUpperCase();
		}
		config.setFieldName(fieldName);

		if (columnAnnotation != null) {
			try {
				Method method = columnAnnotation.getClass().getMethod("name");
				String name = (String) method.invoke(columnAnnotation);
				if (name != null && name.length() > 0) {
					config.setColumnName(name);
				}
				method = columnAnnotation.getClass().getMethod("columnDefinition");
				String columnDefinition = (String) method.invoke(columnAnnotation);
				if (columnDefinition != null && columnDefinition.length() > 0) {
					config.setColumnDefinition(columnDefinition);
				}
				method = columnAnnotation.getClass().getMethod("length");
				config.setWidth((Integer) method.invoke(columnAnnotation));
				method = columnAnnotation.getClass().getMethod("nullable");
				Boolean nullable = (Boolean) method.invoke(columnAnnotation);
				if (nullable != null) {
					config.setCanBeNull(nullable);
				}
				method = columnAnnotation.getClass().getMethod("unique");
				Boolean unique = (Boolean) method.invoke(columnAnnotation);
				if (unique != null) {
					config.setUnique(unique);
				}
			} catch (Exception e) {
				throw SqlExceptionUtil.create(
						"Problem accessing fields from the @Column annotation for field " + field, e);
			}
		}
		if (basicAnnotation != null) {
			try {
				Method method = basicAnnotation.getClass().getMethod("optional");
				Boolean optional = (Boolean) method.invoke(basicAnnotation);
				if (optional == null) {
					config.setCanBeNull(true);
				} else {
					config.setCanBeNull(optional);
				}
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Problem accessing fields from the @Basic annotation for field " + field,
						e);
			}
		}
		if (idAnnotation != null) {
			if (generatedValueAnnotation == null) {
				config.setId(true);
			} else {
				// generatedValue only works if it is also an id according to {@link GeneratedValue)
				config.setGeneratedId(true);
			}
		}
		if (oneToOneAnnotation != null || manyToOneAnnotation != null) {
			// if we have a collection then make it a foreign collection
			if (Collection.class.isAssignableFrom(field.getType())
					|| ForeignCollection.class.isAssignableFrom(field.getType())) {
				config.setForeignCollection(true);
				if (joinColumnAnnotation != null) {
					try {
						Method method = joinColumnAnnotation.getClass().getMethod("name");
						String name = (String) method.invoke(joinColumnAnnotation);
						if (name != null && name.length() > 0) {
							config.setForeignCollectionColumnName(name);
						}
						method = joinColumnAnnotation.getClass().getMethod("fetch");
						Object fetchType = method.invoke(joinColumnAnnotation);
						if (fetchType != null && fetchType.toString().equals("EAGER")) {
							config.setForeignCollectionEager(true);
						}
					} catch (Exception e) {
						throw SqlExceptionUtil.create(
								"Problem accessing fields from the @JoinColumn annotation for field " + field, e);
					}
				}
			} else {
				// otherwise it is a foreign field
				config.setForeign(true);
				if (joinColumnAnnotation != null) {
					try {
						Method method = joinColumnAnnotation.getClass().getMethod("name");
						String name = (String) method.invoke(joinColumnAnnotation);
						if (name != null && name.length() > 0) {
							config.setColumnName(name);
						}
						method = joinColumnAnnotation.getClass().getMethod("nullable");
						Boolean nullable = (Boolean) method.invoke(joinColumnAnnotation);
						if (nullable != null) {
							config.setCanBeNull(nullable);
						}
						method = joinColumnAnnotation.getClass().getMethod("unique");
						Boolean unique = (Boolean) method.invoke(joinColumnAnnotation);
						if (unique != null) {
							config.setUnique(unique);
						}
					} catch (Exception e) {
						throw SqlExceptionUtil.create(
								"Problem accessing fields from the @JoinColumn annotation for field " + field, e);
					}
				}
			}
		}
		if (enumeratedAnnotation != null) {
			try {
				Method method = enumeratedAnnotation.getClass().getMethod("value");
				Object typeValue = method.invoke(enumeratedAnnotation);
				if (typeValue != null && typeValue.toString().equals("STRING")) {
					config.setDataType(DataType.ENUM_STRING);
				} else {
					config.setDataType(DataType.ENUM_INTEGER);
				}
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Problem accessing fields from the @Enumerated annotation for field "
						+ field, e);
			}
		}
		if (versionAnnotation != null) {
			// just the presence of the version...
			config.setVersion(true);
		}
		if (config.getDataPersister() == null) {
			config.setDataPersister(DataPersisterManager.lookupForField(field));
		}
		config.setUseGetSet(DatabaseFieldConfig.findGetMethod(field, false) != null
				&& DatabaseFieldConfig.findSetMethod(field, false) != null);
		return config;
	}

	/**
	 * Return the javax.persistence.Entity annotation name for the class argument or null if none or if there was no
	 * entity name.
	 */
	public static String getEntityName(Class<?> clazz) {
		Annotation entityAnnotation = null;
		for (Annotation annotation : clazz.getAnnotations()) {
			Class<?> annotationClass = annotation.annotationType();
			if (annotationClass.getName().equals("javax.persistence.Entity")) {
				entityAnnotation = annotation;
			}
		}

		if (entityAnnotation == null) {
			return null;
		}
		try {
			Method method = entityAnnotation.getClass().getMethod("name");
			String name = (String) method.invoke(entityAnnotation);
			if (name != null && name.length() > 0) {
				return name;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not get entity name from class " + clazz, e);
		}
	}
}
