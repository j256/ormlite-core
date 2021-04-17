package com.j256.ormlite.logger;

import com.j256.ormlite.logger.Log4jLogBackend.Log4jLogBackendFactory;

public class Log4jLogBackendTest extends BaseLogBackendTest {

	public Log4jLogBackendTest() {
		super(new Log4jLogBackendFactory());
	}
}
