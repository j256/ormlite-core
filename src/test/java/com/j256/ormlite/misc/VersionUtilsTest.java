package com.j256.ormlite.misc;

import org.junit.Test;

public class VersionUtilsTest {

	@Test
	public void testCheckCoreVersusJdbcVersions() {
		VersionUtils.setThrownOnErrors(true);
		VersionUtils.setCoreVersionFile("/coreVersion.txt");
		VersionUtils.setJdbcVersionFile("/jdbcVersion.txt");
		VersionUtils.checkCoreVersusJdbcVersions();
	}

	@Test
	public void testCheckCoreVersusAndroidVersions() {
		VersionUtils.setThrownOnErrors(true);
		VersionUtils.setCoreVersionFile("/coreVersion.txt");
		VersionUtils.setAndroidVersionFile("/androidVersion.txt");
		VersionUtils.checkCoreVersusAndroidVersions();
	}

	@Test(expected = IllegalStateException.class)
	public void testUnknownFiles() {
		VersionUtils.setThrownOnErrors(true);
		VersionUtils.setCoreVersionFile("some/unknown/file/path");
		VersionUtils.checkCoreVersusJdbcVersions();
	}

	@Test
	public void testUnknownFilesNoThrowNoCore() {
		VersionUtils.setThrownOnErrors(false);
		VersionUtils.setCoreVersionFile("some/unknown/file/path");
		VersionUtils.checkCoreVersusJdbcVersions();
	}

	@Test
	public void testUnknownFilesNoThrowNoJdbc() {
		VersionUtils.setThrownOnErrors(false);
		VersionUtils.setCoreVersionFile("/coreVersion.txt");
		VersionUtils.setJdbcVersionFile("some/unknown/file/path");
		VersionUtils.checkCoreVersusJdbcVersions();
	}

	@Test(expected = IllegalStateException.class)
	public void testMismatch() {
		VersionUtils.setThrownOnErrors(true);
		VersionUtils.setCoreVersionFile("/coreVersion.txt");
		VersionUtils.setJdbcVersionFile("/otherVersion.txt");
		VersionUtils.checkCoreVersusJdbcVersions();
	}

	@Test
	public void testMismatchNoThrow() {
		VersionUtils.setThrownOnErrors(false);
		VersionUtils.setCoreVersionFile("/coreVersion.txt");
		VersionUtils.setJdbcVersionFile("/otherVersion.txt");
		VersionUtils.checkCoreVersusJdbcVersions();
	}

	@Test(expected = IllegalStateException.class)
	public void testEmpty() {
		VersionUtils.setThrownOnErrors(true);
		VersionUtils.setCoreVersionFile("/emptyVersion.txt");
		VersionUtils.checkCoreVersusJdbcVersions();
	}

	@Test
	public void testEmptyNoThrow() {
		VersionUtils.setThrownOnErrors(false);
		VersionUtils.setCoreVersionFile("/emptyVersion.txt");
		VersionUtils.setJdbcVersionFile("/jdbcVersion.txt");
		VersionUtils.checkCoreVersusJdbcVersions();
	}
}
