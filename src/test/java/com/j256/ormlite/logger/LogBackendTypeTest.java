package com.j256.ormlite.logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
				assertFalse(type.isAvailable(), type + " should not be available");
				// NOTE: type.createLogBackend() defers to LocalLog
				continue;
			}

			assertTrue(type.isAvailable(), type + " should be available");
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
				assertFalse(LogBackendType.isAvailable(type), type + " should not be available");
				// NOTE: type.createLogBackend() defers to LocalLog
				continue;
			}

			assertTrue(LogBackendType.isAvailable(type), type + " should be available");
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
