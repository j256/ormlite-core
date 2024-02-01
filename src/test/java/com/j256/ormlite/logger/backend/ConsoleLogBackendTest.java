package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.backend.ConsoleLogBackend.ConsoleLogBackendFactory;

public class ConsoleLogBackendTest extends BaseLogBackendTest {

	public ConsoleLogBackendTest() {
		super(new ConsoleLogBackendFactory());
	}
}
