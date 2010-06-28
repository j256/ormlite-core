package com.j256.ormlite.db;

import java.util.List;

import com.j256.ormlite.field.FieldType;

/**
 * Postgres database type information used to create the tables, etc..
 * 
 * @author graywatson
 */
public class PostgresDatabaseType extends BaseDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "postgresql";
	private final static String DRIVER_CLASS_NAME = "org.postgresql.Driver";

	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	protected void appendByteType(StringBuilder sb) {
		sb.append("SMALLINT");
	}

	@Override
	protected void appendObjectType(StringBuilder sb) {
		sb.append("BYTEA");
	}

	@Override
	protected void configureGeneratedIdSequence(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		String sequenceName = fieldType.getGeneratedIdSequence();
		// needs to match dropColumnArg()
		StringBuilder seqSb = new StringBuilder();
		seqSb.append("CREATE SEQUENCE ");
		// when it is created, it needs to be escaped specially
		appendEscapedEntityName(seqSb, sequenceName);
		statementsBefore.add(seqSb.toString());

		sb.append("DEFAULT NEXTVAL(");
		// when it is used, it is escaped as a word, grumble
		appendEscapedWordEntityName(sb, sequenceName);
		sb.append(") ");
		// could also be the type serial for auto-generated sequences
		// 8.2 also have the returning insert statement

		configureId(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
	}

	@Override
	public void dropColumnArg(FieldType fieldType, List<String> statementsBefore, List<String> statementsAfter) {
		if (fieldType.isGeneratedIdSequence()) {
			StringBuilder sb = new StringBuilder();
			sb.append("DROP SEQUENCE ");
			appendEscapedEntityName(sb, fieldType.getGeneratedIdSequence());
			statementsAfter.add(sb.toString());
		}
	}

	@Override
	public void appendEscapedEntityName(StringBuilder sb, String word) {
		sb.append('\"').append(word).append('\"');
	}

	public void appendEscapedWordEntityName(StringBuilder sb, String word) {
		// postgres needed this special escaping for NEXTVAL('"sequence-name"')
		sb.append('\'').append('\"').append(word).append('\"').append('\'');
	}

	@Override
	public boolean isIdSequenceNeeded() {
		return true;
	}

	@Override
	public void appendSelectNextValFromSequence(StringBuilder sb, String sequenceName) {
		sb.append("SELECT NEXTVAL(");
		// this is word and not entity unfortunately
		appendEscapedWord(sb, sequenceName);
		sb.append(')');
	}
}
