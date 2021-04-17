package com.j256.ormlite.logger;

import com.j256.ormlite.logger.LogbackLogBackend.LogbackLogBackendFactory;

public class LogbackLogBackendTest extends BaseLogBackendTest {

	public LogbackLogBackendTest() {
		super(new LogbackLogBackendFactory());
	}
}
