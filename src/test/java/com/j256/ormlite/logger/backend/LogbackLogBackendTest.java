package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.backend.LogbackLogBackend.LogbackLogBackendFactory;

public class LogbackLogBackendTest extends BaseLogBackendTest {

	public LogbackLogBackendTest() {
		super(new LogbackLogBackendFactory());
	}
}
