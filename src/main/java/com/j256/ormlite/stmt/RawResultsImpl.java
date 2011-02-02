package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Handler for our raw results objects which does the conversion for various different results: String[], Object[], and
 * user defined <T>.
 * 
 * @author graywatson
 */
public class RawResultsImpl<T> implements GenericRawResults<T> {

	protected final GenericRowMapper<T> rowMapper;
	protected SelectIterator<T, Void> iterator;
	protected final String[] columnNames;

	public RawResultsImpl(ConnectionSource connectionSource, DatabaseConnection connection, String query,
			Class<?> clazz, CompiledStatement compiledStmt, String[] columnNames, GenericRowMapper<T> rowMapper) throws SQLException {
		this.rowMapper = rowMapper;
		iterator =
				new SelectIterator<T, Void>(clazz, null, rowMapper, connectionSource, connection, compiledStmt, query);
		this.columnNames = columnNames;
	}

	public int getNumberColumns() {
		return columnNames.length;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public List<T> getResults() throws SQLException {
		List<T> results = new ArrayList<T>();
		try {
			while (iterator.hasNext()) {
				results.add(iterator.next());
			}
			return results;
		} finally {
			iterator.close();
		}
	}

	public CloseableIterator<T> iterator() {
		return iterator;
	}

	public void close() throws SQLException {
		if (iterator != null) {
			iterator.close();
			iterator = null;
		}
	}
}
