package com.j256.ormlite.logger;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.jupiter.api.Test;

public class BaseLoggerTest {

	@Test
	public void testZeroLengthArgs() {
		LogBackend backend = createMock(LogBackend.class);
		OurLogger logger = new OurLogger(backend);

		Level level = Level.TRACE;
		backend.log(level, BaseLogger.NO_MESSAGE_MESSAGE);

		replay(backend);
		/*
		 * Verify specific error which probably can't be triggered but I wanted to simulate it here. When there is no
		 * message so we build the args message but there is an object array of 0 size we were getting a string-builder
		 * array out of bounds issue because of a argCount++ in the wrong place.
		 */
		logger.doLog(level, null, null, new Object[0], 0);
		verify(backend);
	}

	private static class OurLogger extends BaseLogger {
		public OurLogger(LogBackend backend) {
			super(backend);
		}
	}
}
