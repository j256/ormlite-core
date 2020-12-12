package com.j256.ormlite.logger;

import org.junit.Test;

import com.j256.ormlite.logger.NullLog.NullLogFactory;

public class NullLogTest extends BaseLogTest {

	public NullLogTest() {
		super(new NullLog(null));
	}

	@Test
	public void testStuff() {
		LoggerFactory.setLogFactory(new NullLogFactory());
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.fatal("shouldn't see this");
	}
}
