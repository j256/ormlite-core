package com.j256.ormlite.field;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;

public class DatabaseFieldConfigReflectionTest extends BaseCoreTest {

	@Test
	public void testAnnotationReflection() throws Exception {
		// run through @DatabaseField, look for set/get in DatabaseFieldConfig
		for (Method method : DatabaseField.class.getMethods()) {
			String methodName;
			if (method.getName().equals("unknownEnumName")) {
				methodName = "unknownEnumValue";
			} else {
				methodName = method.getName();
			}

			Class<?> getArg;
			if (methodName.equals("indexName") || methodName.equals("uniqueIndexName")) {
				getArg = String.class;
			} else {
				getArg = null;
			}

			Class<?> setArg;
			if (methodName.equals("unknownEnumValue")) {
				setArg = Enum.class;
			} else {
				setArg = method.getReturnType();
			}

			testAnnotationMethod(method, null, methodName, getArg, setArg);
		}

		// run through @ForeignCollectionField, look for set/get in DatabaseFieldConfig
		for (Method method : ForeignCollectionField.class.getMethods()) {
			testAnnotationMethod(method, "foreignCollection", method.getName(), null, method.getReturnType());
		}
	}

	private void testAnnotationMethod(Method method, String methodNamePrefix, String methodName, Class<?> getArg,
			Class<?> setArg) throws NoSuchMethodException {
		if (methodName.equals("equals") || methodName.equals("toString") || methodName.equals("hashCode")
				|| methodName.equals("annotationType")) {
			return;
		}
		if (methodNamePrefix != null) {
			methodName = methodNamePrefix + StringUtils.capitalize(methodName);
		}
		// test getter
		if (method.getAnnotation(Deprecated.class) == null) {
			if (method.getReturnType() == boolean.class) {
				DatabaseFieldConfig.class.getMethod("is" + StringUtils.capitalize(methodName));
			} else {
				if (getArg == null) {
					DatabaseFieldConfig.class.getMethod("get" + StringUtils.capitalize(methodName));
				} else {
					DatabaseFieldConfig.class.getMethod("get" + StringUtils.capitalize(methodName), getArg);
				}
			}
		}
		// test setter
		DatabaseFieldConfig.class.getMethod("set" + StringUtils.capitalize(methodName), setArg);
	}
}
