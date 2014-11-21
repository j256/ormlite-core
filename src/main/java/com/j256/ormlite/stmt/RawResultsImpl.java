package com.j256.ormlite.stmt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Handler for our raw results objects which does the conversion for various different results: String[], Object[], and
 * user defined {@code <T>}.
 * 
 * @author graywatson
 */
public class RawResultsImpl<T> implements GenericRawResults<T> {

	private SelectIterator<T, Void> iterator;
	private final String[] columnNames;

	public RawResultsImpl(ConnectionSource connectionSource, DatabaseConnection connection, String query,
			Class<?> clazz, CompiledStatement compiledStmt, GenericRowMapper<T> rowMapper, ObjectCache objectCache)
			throws SQLException {
		iterator =
				new SelectIterator<T, Void>(clazz, null, rowMapper, connectionSource, connection, compiledStmt, query,
						objectCache);
		/*
		 * NOTE: we _have_ to get these here before the results object is closed if there are no results
		 */
		this.columnNames = iterator.getRawResults().getColumnNames();
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
			IOUtils.closeThrowSqlException(this, "raw results iterator");
		}
	}

	public T getFirstResult() throws SQLException {
		try {
			if (iterator.hasNextThrow()) {
				return iterator.nextThrow();
			} else {
				return null;
			}
		} finally {
			IOUtils.closeThrowSqlException(this, "raw results iterator");
		}
	}

	public CloseableIterator<T> iterator() {
		return iterator;
	}

	public CloseableIterator<T> closeableIterator() {
		return iterator;
	}

	public void close() throws IOException {
		if (iterator != null) {
			iterator.close();
			iterator = null;
		}
	}
}
