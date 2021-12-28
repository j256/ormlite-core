package com.j256.ormlite.logger.backend;

import org.junit.Ignore;

import com.j256.ormlite.logger.backend.Log4j2LogBackend.Log4j2LogBackendFactory;

@Ignore("ignored because this requires java8")
public class Log4j2LogBackendTest extends BaseLogBackendTest {

	public Log4j2LogBackendTest() {
		super(new Log4j2LogBackendFactory());
	}
}
