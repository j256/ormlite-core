package com.j256.ormlite.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LevelTest {

	@Test
	public void testCoverage() {
		assertFalse(Level.OFF.isEnabled(Level.TRACE));
		assertFalse(Level.TRACE.isEnabled(Level.OFF));
		assertTrue(Level.TRACE.isEnabled(Level.INFO));
		assertFalse(Level.INFO.isEnabled(Level.TRACE));
	}

	@Test
	public void testFromString() {
		assertNull(Level.fromString(null));
		assertNull(Level.fromString(""));
		for (Level level : Level.values()) {
			assertEquals(level, Level.fromString(level.name()));
			assertEquals(level, Level.fromString(level.name().toLowerCase()));
		}
		assertNull(Level.fromString("unknown"));
	}
}
