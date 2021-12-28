package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.backend.Slf4jLoggingLogBackend.Slf4jLoggingLogBackendFactory;

public class Slf4jLoggingLogBackendTest extends BaseLogBackendTest {

	public Slf4jLoggingLogBackendTest() {
		super(new Slf4jLoggingLogBackendFactory());
	}
}
