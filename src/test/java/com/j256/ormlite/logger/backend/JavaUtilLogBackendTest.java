package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.BaseLogBackendTest;
import com.j256.ormlite.logger.backend.JavaUtilLogBackend.JavaUtilLogBackendFactory;

public class JavaUtilLogBackendTest extends BaseLogBackendTest {

	public JavaUtilLogBackendTest() {
		super(new JavaUtilLogBackendFactory());
	}
}
