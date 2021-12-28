package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.BaseLogBackendTest;
import com.j256.ormlite.logger.backend.Log4jLogBackend.Log4jLogBackendFactory;

public class Log4jLogBackendTest extends BaseLogBackendTest {

	public Log4jLogBackendTest() {
		super(new Log4jLogBackendFactory());
	}
}
