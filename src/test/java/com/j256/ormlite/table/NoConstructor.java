package com.j256.ormlite.table;

import org.junit.Ignore;

import com.j256.ormlite.field.DatabaseField;

/**
 * We have to have this here so the constructor can be invisible to the {@link DatabaseTableConfigTest}.
 * 
 * @author graywatson
 */
@Ignore
public class NoConstructor {
	@DatabaseField
	String stuff;
	public NoConstructor(String notNoArg) {
	}
}
