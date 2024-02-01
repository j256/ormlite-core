package com.j256.ormlite.logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
}
