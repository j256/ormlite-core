package com.j256.ormlite.logger;

import com.j256.ormlite.logger.JavaUtilLogBackend.JavaUtilLogBackendFactory;

public class JavaUtilLogBackendTest extends BaseLogBackendTest {

	public JavaUtilLogBackendTest() {
		super(new JavaUtilLogBackendFactory());
	}
}
