package com.j256.ormlite;

import java.io.File;

import org.junit.Ignore;

import com.j256.ormlite.db.DatabaseType;

/**
 * Set of test utilities for all of the unit tests.
 * 
 * @author graywatson
 */
@Ignore("Test utilities and not tests")
public class TestUtils {

	public static void deleteDirectory(File directory) {
		if (!directory.exists()) {
			return;
		}
		if (directory.isFile()) {
			directory.delete();
			return;
		}
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				deleteDirectory(file);
			}
			file.delete();
		}
	}

	public static String appendEscapedEntityName(DatabaseType databaseType, String word) {
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, word);
		return sb.toString();
	}

	public static String appendEscapedWord(DatabaseType databaseType, String word) {
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedWord(sb, word);
		return sb.toString();
	}
}
