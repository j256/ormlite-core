package com.j256.ormlite.logger.backend;

import org.junit.Test;

import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.logger.backend.NullLogBackend.NullLogBackendFactory;

public class NullLogBackendTest extends BaseLogBackendTest {

	public NullLogBackendTest() {
		super(new NullLogBackendFactory());
	}

	@Test
	public void testStuff() {
		LoggerFactory.setLogBackendFactory(new NullLogBackendFactory());
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.fatal("shouldn't see this");
	}
}
