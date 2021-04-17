package com.j256.ormlite.logger;

import com.j256.ormlite.logger.CommonsLoggingLogBackend.CommonsLoggingLogBackendFactory;

public class CommonsLoggingLogBackendTest extends BaseLogBackendTest {

	public CommonsLoggingLogBackendTest() {
		super(new CommonsLoggingLogBackendFactory());
	}
}
