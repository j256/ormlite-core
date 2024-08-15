package com.j256.ormlite.field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

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

	@Test
	public void testAnnotationConfigLoadReflection() throws Exception {
		// each field should have a setting
		for (Method method : DatabaseField.class.getMethods()) {
			if (method.getName().equals("indexName") || method.getName().equals("uniqueIndexName")
					|| method.getName().equals("unknownEnumName")) {
				continue;
			}
			testMethod(method, null);
		}

		// each field should have a setting
		for (Method method : ForeignCollectionField.class.getMethods()) {
			testMethod(method, "foreignCollection");
		}
	}

	private void testMethod(Method method, String methodNamePrefix) throws Exception {
		String methodName = method.getName();
		if (isObjectMethodName(methodName)) {
			return;
		}
		if (methodName.equals("maxForeignAutoRefreshLevel")) {
			return;
		}
		if (methodNamePrefix != null) {
			methodName = methodNamePrefix + capitalize(methodName);
		}
		Class<?> clazz = method.getReturnType();
		String line;
		Object equalsValue;
		Object notEqualsValue;
		if (clazz == boolean.class) {
			equalsValue = true;
			notEqualsValue = false;
		} else if (clazz == int.class) {
			equalsValue = 123413;
			notEqualsValue = (Integer) equalsValue + 1;
		} else if (clazz == String.class) {
			equalsValue = "pwjpweojfwefw";
			notEqualsValue = (String) equalsValue + "foo";
		} else {
			System.out.println("Skipping unknown value class " + clazz);
			return;
		}
		line = methodName + "=" + equalsValue;
		DatabaseFieldConfig config = testConfigLine(line);
		Method getMethod;
		if (method.getReturnType() == boolean.class) {
			getMethod = DatabaseFieldConfig.class.getMethod("is" + capitalize(methodName));
		} else {
			getMethod = DatabaseFieldConfig.class.getMethod("get" + capitalize(methodName));
		}
		Object methodValue = getMethod.invoke(config);
		assertEquals(equalsValue, methodValue, methodName);
		assertFalse(notEqualsValue.equals(methodValue),
				methodName + " not-equals " + notEqualsValue + " should not be == " + methodValue);
	}

	private DatabaseFieldConfig testConfigLine(String line) throws SQLException {
		StringBuffer sb = new StringBuffer();
		sb.append("fieldName=foo\n");
		sb.append(line).append('\n');
		DatabaseFieldConfig config =
				DatabaseFieldConfigLoader.fromReader(new BufferedReader(new StringReader(sb.toString())));
		assertNotNull(config);
		return config;
	}

	private void testAnnotationMethod(Method method, String methodNamePrefix, String methodName, Class<?> getArg,
			Class<?> setArg) throws NoSuchMethodException {
		if (isObjectMethodName(methodName)) {
			return;
		}
		if (methodNamePrefix != null) {
			methodName = methodNamePrefix + capitalize(methodName);
		}
		// test getter
		if (method.getReturnType() == boolean.class) {
			DatabaseFieldConfig.class.getMethod("is" + capitalize(methodName));
		} else {
			if (getArg == null) {
				DatabaseFieldConfig.class.getMethod("get" + capitalize(methodName));
			} else {
				DatabaseFieldConfig.class.getMethod("get" + capitalize(methodName), getArg);
			}
		}
		// test setter
		DatabaseFieldConfig.class.getMethod("set" + capitalize(methodName), setArg);
	}

	private boolean isObjectMethodName(String name) {
		return (name.equals("equals") || name.equals("toString") || name.equals("hashCode")
				|| name.equals("annotationType"));
	}

	private String capitalize(String str) {
		StringBuilder sb = new StringBuilder(str.length());
		sb.append(Character.toUpperCase(str.charAt(0)));
		sb.append(str, 1, str.length());
		return sb.toString();
	}
}
