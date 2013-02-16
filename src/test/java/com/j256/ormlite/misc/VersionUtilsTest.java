package com.j256.ormlite.misc;

import org.junit.Test;

public class VersionUtilsTest {

	@Test
	public void testCheckCoreVersusJdbcVersionsGood() {
		VersionUtils.setThrownOnErrors(true);
		VersionUtils.checkCoreVersusJdbcVersions(VersionUtils.getCoreVersion());
	}

	@Test(expected = IllegalStateException.class)
	public void testCheckCoreVersusJdbcVersionsBad() {
		VersionUtils.setThrownOnErrors(true);
		VersionUtils.checkCoreVersusJdbcVersions("xxx");
	}

	@Test
	public void testCheckCoreVersusAndroidVersionsGood() {
		VersionUtils.setThrownOnErrors(true);
		VersionUtils.checkCoreVersusAndroidVersions(VersionUtils.getCoreVersion());
	}

	@Test(expected = IllegalStateException.class)
	public void testCheckCoreVersusAndroidVersionsBad() {
		VersionUtils.setThrownOnErrors(true);
		VersionUtils.checkCoreVersusAndroidVersions("xxx");
	}
}
