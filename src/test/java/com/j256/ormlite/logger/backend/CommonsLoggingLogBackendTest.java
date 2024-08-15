package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.backend.CommonsLoggingLogBackend.CommonsLoggingLogBackendFactory;

public class CommonsLoggingLogBackendTest extends BaseLogBackendTest {

	public CommonsLoggingLogBackendTest() {
		super(new CommonsLoggingLogBackendFactory());
	}
}
