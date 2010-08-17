package com.j256.ormlite.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.j256.ormlite.field.JdbcType;
import com.j256.ormlite.stmt.GenericRowMapper;

/**
 * Implementation of {@link JdbcTemplate} interface to replace Spring's JdbcTemplate.
 * 
 * @author graywatson
 */
public class JdbcTemplateImpl implements JdbcTemplate {

	private static Object[] noArgs = new Object[0];
	private static int[] noArgTypes = new int[0];
	private static GenericRowMapper<Long> longWrapper = new OneLongWrapper();

	private DataSource dataSource;

	/**
	 * Construct a jdbc template with an associated data source.
	 */
	public JdbcTemplateImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public int update(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException {
		PreparedStatement stmt = dataSource.getConnection().prepareStatement(statement);
		statementSetArgs(stmt, args, argFieldTypeVals);
		return stmt.executeUpdate();
	}

	public int update(String statement, Object[] args, int[] argFieldTypeVals, GeneratedKeyHolder keyHolder)
			throws SQLException {
		PreparedStatement stmt =
				dataSource.getConnection().prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
		statementSetArgs(stmt, args, argFieldTypeVals);
		int rowN = stmt.executeUpdate();
		ResultSet results = stmt.getGeneratedKeys();
		if (results == null) {
			// may never happen but let's be careful
			throw new SQLException("No generated key results returned from update: " + statement);
		}
		ResultSetMetaData metaData = results.getMetaData();
		int colN = metaData.getColumnCount();
		while (results.next()) {
			for (int colC = 1; colC <= colN; colC++) {
				String colName = metaData.getColumnName(colC);
				int typeVal = metaData.getColumnType(colC);
				JdbcType jdbcType = JdbcType.lookupIdTypeVal(typeVal);
				Number id = jdbcType.resultToId(results, colC);
				if (id == null) {
					// may never happen but let's be careful
					throw new SQLException("Generated column " + colName + " is invalid type " + jdbcType + ", value "
							+ typeVal);
				} else {
					keyHolder.addKey(colName, id);
				}
			}
		}
		return rowN;
	}

	public <T> Object queryForOne(String statement, Object[] args, int[] argFieldTypeVals, GenericRowMapper<T> rowMapper)
			throws SQLException {
		PreparedStatement stmt =
				dataSource.getConnection().prepareStatement(statement, ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
		statementSetArgs(stmt, args, argFieldTypeVals);
		ResultSet results = stmt.executeQuery();
		if (!results.next()) {
			// no results at all
			return null;
		}
		T first = rowMapper.mapRow(results, 0);
		if (results.next()) {
			return MORE_THAN_ONE;
		} else {
			return first;
		}
	}

	public long queryForLong(String statement) throws SQLException {
		Object result = queryForOne(statement, noArgs, noArgTypes, longWrapper);
		if (result == null) {
			throw new SQLException("No results returned in query-for-long: " + statement);
		} else if (result == MORE_THAN_ONE) {
			throw new SQLException("More thank 1 result returned in query-for-long: " + statement);
		} else {
			return (Long) result;
		}
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return dataSource.getConnection().prepareStatement(sql);
	}

	private void statementSetArgs(PreparedStatement stmt, Object[] args, int[] argFieldTypeVals) throws SQLException {
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg == null) {
				stmt.setNull(i + 1, argFieldTypeVals[i]);
			} else {
				stmt.setObject(i + 1, arg, argFieldTypeVals[i]);
			}
		}
	}

	/**
	 * Row mapper that handles a single long result.
	 */
	private static class OneLongWrapper implements GenericRowMapper<Long> {
		public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
			// maps the first column (sql #1)
			return rs.getLong(1);
		}
	}
}
