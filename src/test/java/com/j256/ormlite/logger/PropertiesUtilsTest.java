package com.j256.ormlite.logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;

import com.j256.ormlite.logger.PropertyUtils.PatternLevel;

public class PropertiesUtilsTest {

	@AfterClass
	public static void afterClass() {
		Logger.setGlobalLogLevel(null);
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(null);
	}

	@Test
	public void testReadFromFile() {
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(null);
		String backend = PropertyUtils.readBackendTypeClassProperty(LogBackendType.LOCAL);
		assertEquals(LogBackendType.LOCAL.name(), backend);
	}

	@Test
	public void testTypeClass() {
		StringWriter stringWriter = new StringWriter();
		String value = "fpoejwpjefw";
		stringWriter.write(PropertyUtils.BACKEND_TYPE_CLASS_PROPERTY + " = " + value + "\n");
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		String backend = PropertyUtils.readBackendTypeClassProperty(LogBackendType.LOCAL);
		assertEquals(value, backend);
	}

	@Test
	public void testTypeClassEmpty() {
		StringWriter stringWriter = new StringWriter();
		stringWriter.write("something = else\n");
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		String backend = PropertyUtils.readBackendTypeClassProperty(LogBackendType.LOCAL);
		assertNull(backend);
	}

	@Test
	public void testDiscoveryOrder() {
		StringWriter stringWriter = new StringWriter();
		stringWriter.write(PropertyUtils.DISCOVERY_ORDER_PROPERTY + " = " + LogBackendType.LOCAL + ","
				+ LogBackendType.NULL + "\n");
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		LogBackendType[] order = PropertyUtils.readDiscoveryOrderProperty(LogBackendType.LOCAL);
		assertEquals(2, order.length);
		assertEquals(LogBackendType.LOCAL, order[0]);
		assertEquals(LogBackendType.NULL, order[1]);
	}

	@Test
	public void testDiscoveryOrderInvalid() {
		StringWriter stringWriter = new StringWriter();
		String value = "weffewewf";
		stringWriter.write("something = else\n");
		stringWriter.write(PropertyUtils.DISCOVERY_ORDER_PROPERTY + " = " + value + "\n");
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		LogBackendType[] order = PropertyUtils.readDiscoveryOrderProperty(LogBackendType.LOCAL);
		assertNull(order);
	}

	@Test
	public void testDiscoveryOrderNone() {
		StringWriter stringWriter = new StringWriter();
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		LogBackendType[] order = PropertyUtils.readDiscoveryOrderProperty(LogBackendType.LOCAL);
		assertNull(order);
	}

	@Test
	public void testAssignGlobalLevel() {
		StringWriter stringWriter = new StringWriter();
		Level level = Level.WARNING;
		stringWriter.write("something=else\n");
		stringWriter.write(PropertyUtils.GLOBAL_LEVEL_PROPERTY + " = " + level.name() + "\n");
		Logger.setGlobalLogLevel(null);
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		PropertyUtils.assignGlobalLevelFromProperty(LogBackendType.LOCAL);
		assertEquals(level, Logger.getGlobalLevel());
	}

	@Test
	public void testAssignGlobalLevelInvalid() {
		StringWriter stringWriter = new StringWriter();
		String invalid = "powejfwefewf";
		stringWriter.write(PropertyUtils.GLOBAL_LEVEL_PROPERTY + " = " + invalid + "\n");
		Level level = Level.ERROR;
		Logger.setGlobalLogLevel(level);
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		PropertyUtils.assignGlobalLevelFromProperty(LogBackendType.LOCAL);
		assertEquals(level, Logger.getGlobalLevel());
	}

	@Test
	public void testAssignGlobalLevelEmpty() {
		StringWriter stringWriter = new StringWriter();
		stringWriter.write(PropertyUtils.GLOBAL_LEVEL_PROPERTY + "=\n");
		Level level = Level.ERROR;
		Logger.setGlobalLogLevel(level);
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		PropertyUtils.assignGlobalLevelFromProperty(LogBackendType.LOCAL);
		assertEquals(level, Logger.getGlobalLevel());
	}

	@Test
	public void testAssignGlobalLevelNull() {
		StringWriter stringWriter = new StringWriter();
		stringWriter.write(PropertyUtils.GLOBAL_LEVEL_PROPERTY + "=" + PropertyUtils.GLOBAL_LEVEL_NULL_VALUE + "\n");
		Logger.setGlobalLogLevel(Level.ERROR);
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		PropertyUtils.assignGlobalLevelFromProperty(LogBackendType.LOCAL);
		PropertyUtils.assignGlobalLevelFromProperty(LogBackendType.LOCAL);
		assertNull(Logger.getGlobalLevel());
	}

	@Test
	public void testInvalidLevelsFile() {
		StringWriter stringWriter = new StringWriter();
		// invalid line
		stringWriter.write("x\n");
		// invalid level
		stringWriter.write(
				PropertyUtils.LOCAL_LOG_PROPERTY_PREFIX + "com\\.foo\\.myclass\\.StatementExecutor = INVALID_LEVEL\n");
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		List<PatternLevel> logPatterns = PropertyUtils.readLocalLogPatterns(LogBackendType.LOCAL);
		assertNull(logPatterns);
	}

	@Test
	public void testEmptyLevelPattern() {
		StringWriter stringWriter = new StringWriter();
		// invalid line
		stringWriter.write("x\n");
		// invalid level
		stringWriter.write(PropertyUtils.LOCAL_LOG_PROPERTY_PREFIX + " = INFO\n");
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		List<PatternLevel> logPatterns = PropertyUtils.readLocalLogPatterns(LogBackendType.LOCAL);
		assertNull(logPatterns);
	}

	@Test
	public void testValidLevelsFile() {
		StringWriter stringWriter = new StringWriter();
		// invalid line
		stringWriter.write("x\n");
		// blank line
		stringWriter.write("\n");
		// valid level
		String pattern = "com\\.foo\\.myclass\\.StatementExecutor";
		Level level = Level.INFO;
		stringWriter.write(PropertyUtils.LOCAL_LOG_PROPERTY_PREFIX + pattern + " = " + level.name() + "\n");
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		List<PatternLevel> logPatterns = PropertyUtils.readLocalLogPatterns(LogBackendType.LOCAL);
		assertEquals(1, logPatterns.size());
		assertEquals(pattern, logPatterns.get(0).getPattern().toString());
		assertEquals(level, logPatterns.get(0).getLevel());
	}

	@Test
	public void testIoErrorsReadingLevelFile() {
		InputStream errorStream = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("simulated exception");
			}

			@Override
			public void close() throws IOException {
				throw new IOException("simulated exception");
			}
		};
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(errorStream);
		PropertyUtils.readLocalLogPatterns(LogBackendType.LOCAL);
	}

	@Test
	public void testOrder() {
		StringWriter stringWriter = new StringWriter();
		// invalid line
		stringWriter.write("x\n");
		// blank line
		stringWriter.write("\n");
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		LogBackendType[] order = PropertyUtils.readDiscoveryOrderProperty(LogBackendType.LOCAL);
		assertNull(order);
	}

	@Test
	public void testCoverage() {
		StringWriter stringWriter = new StringWriter();
		// invalid line
		stringWriter.write("=\n");
		stringWriter.write("x=\n");
		PropertyUtils.clearProperties();
		PropertyUtils.setPropertiesInputStream(new ByteArrayInputStream(stringWriter.toString().getBytes()));
		LogBackendType[] order = PropertyUtils.readDiscoveryOrderProperty(LogBackendType.LOCAL);
		assertNull(order);
	}

	@Test
	public void testOrderStringProcessing() {
		assertNull(PropertyUtils.processDiscoveryOrderValue(null, LogBackendType.LOCAL));
		assertNull(PropertyUtils.processDiscoveryOrderValue("", LogBackendType.LOCAL));
	}
}
