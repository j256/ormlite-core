package com.j256.ormlite.logger;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

public class LogArgumentCreatorTest {

	private Logger logger;
	private LogBackend mockBackend;

	@Before
	public void before() {
		mockBackend = createMock(LogBackend.class);
		Logger.setGlobalLogLevel(null);
		logger = new Logger(mockBackend);
		assertSame(mockBackend, logger.getLogBackend());
	}

	@Test
	public void testArgAtStart() {
		String arg = "x";
		String end = " yyy";
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(false);
		expect(mockBackend.isLevelEnabled(Level.INFO)).andReturn(true);
		mockBackend.log(Level.INFO, arg + end);
		LogArgumentCreator logMessageCreator1 = createMock(LogArgumentCreator.class);
		// no calls to logMessageCreator1
		LogArgumentCreator logMessageCreator2 = createMock(LogArgumentCreator.class);
		expect(logMessageCreator2.createLogArg()).andReturn(arg);
		replay(mockBackend, logMessageCreator1, logMessageCreator2);
		// this shouldn't be logged which means no calls to the creator
		logger.trace("{}" + end, logMessageCreator1);
		// this should be logged
		logger.info("{}" + end, logMessageCreator2);
		verify(mockBackend, logMessageCreator1, logMessageCreator2);
	}
}
