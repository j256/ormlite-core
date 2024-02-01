package com.j256.ormlite.logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LevelTest {

	@Test
	public void testCoverage() {
		assertFalse(Level.OFF.isEnabled(Level.TRACE));
		assertFalse(Level.TRACE.isEnabled(Level.OFF));
		assertTrue(Level.TRACE.isEnabled(Level.INFO));
		assertFalse(Level.INFO.isEnabled(Level.TRACE));
	}
}
