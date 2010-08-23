package com.j256.ormlite.db;

import java.util.List;

import com.j256.ormlite.field.FieldType;

/**
 * Oracle database type information used to create the tables, etc..
 * 
 * <p>
 * <b>WARNING:</b> I have not tested this unfortunately because of a lack of access to a Oracle instance. Love to get
 * 1-2 hours of access to an database to test/tweak this. Undoubtably is it wrong. Please contact the author if you'd
 * like to help.
 * </p>
 * 
 * @author graywatson
 */
public class OracleDatabaseType extends BaseDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "oracle";
	private final static String DRIVER_CLASS_NAME = "oracle.jdbc.driver.OracleDriver";

	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	protected void appendStringType(StringBuilder sb, int fieldWidth) {
		sb.append("VARCHAR2(").append(fieldWidth).append(")");
	}

	@Override
	protected void appendByteType(StringBuilder sb) {
		sb.append("SMALLINT");
	}

	@Override
	protected void appendLongType(StringBuilder sb) {
		sb.append("NUMERIC");
	}

	@Override
	protected void appendObjectType(StringBuilder sb) {
		sb.append("LONG RAW");
	}

	@Override
	protected void configureGeneratedIdSequence(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		String seqName = fieldType.getGeneratedIdSequence();
		// needs to match dropColumnArg()
		StringBuilder seqSb = new StringBuilder();
		seqSb.append("CREATE SEQUENCE ");
		// when it is created, it needs to be escaped specially
		appendEscapedEntityName(seqSb, seqName);
		statementsBefore.add(seqSb.toString());

		configureId(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
	}

	@Override
	protected void configureId(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		sb.append("PRIMARY KEY ");
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

	@Override
	public boolean isIdSequenceNeeded() {
		return true;
	}

	@Override
	public void appendSelectNextValFromSequence(StringBuilder sb, String sequenceName) {
		sb.append("SELECT ");
		// this may not work -- may need to have no escape
		appendEscapedEntityName(sb, sequenceName);
		// dual is some sort of special internal table I think
		sb.append(".nextval FROM dual");
	}
}
