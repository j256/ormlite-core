package com.j256.ormlite.logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.logger.backend.NullLogBackend.NullLogBackendFactory;

public class LogBackendTypeTest {

	@Test
	public void testBackends() {
		for (LogBackendType type : LogBackendType.values()) {
			if (type == LogBackendType.LOG4J) {
				// we have to skip it because it is only enabled with a certain profile
				continue;
			}
			if (type == LogBackendType.ANDROID || type == LogBackendType.NULL || type == LogBackendType.LAMBDA) {
				assertFalse(type + " should not be available", type.isAvailable());
				// NOTE: type.createLogBackend() defers to LocalLog
				continue;
			}

			assertTrue(type + " should be available", type.isAvailable());
			assertNotNull(type.createLogBackend(getClass().getSimpleName()));
		}
	}

	@Test
	public void testBackendsAvailable() {
		for (LogBackendType type : LogBackendType.values()) {
			if (type == LogBackendType.LOG4J) {
				// we have to skip it because it is only enabled with a certain profile
				continue;
			}
			if (type == LogBackendType.ANDROID || type == LogBackendType.NULL || type == LogBackendType.LAMBDA) {
				assertFalse(type + " should not be available", LogBackendType.isAvailable(type));
				// NOTE: type.createLogBackend() defers to LocalLog
				continue;
			}

			assertTrue(type + " should be available", LogBackendType.isAvailable(type));
		}
	}

	@Test
	public void testCustomBackendFactoryAvailable() {
		assertTrue(LogBackendType.isAvailable(new LocalBackendFactory(true, false)));
		assertFalse(LogBackendType.isAvailable(new LocalBackendFactory(false, false)));
		assertFalse(LogBackendType.isAvailable(new LocalBackendFactory(true, true)));
	}

	private static class LocalBackendFactory implements LogBackendFactory {

		private final boolean available;
		private final boolean throwOnCreate;

		public LocalBackendFactory(boolean available, boolean throwOnCreate) {
			this.available = available;
			this.throwOnCreate = throwOnCreate;
		}

		@Override
		public boolean isAvailable() {
			return available;
		}

		@Override
		public LogBackend createLogBackend(String classLabel) {
			if (throwOnCreate) {
				throw new IllegalArgumentException("simulated failure");
			} else {
				return NullLogBackendFactory.getSingleton().createLogBackend(classLabel);
			}
		}
	}
}
