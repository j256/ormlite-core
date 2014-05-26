package com.j256.ormlite.support;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Savepoint;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;

/**
 * Database connection proxy so you can intercept database operations either for debugging, replication, logging, or
 * other purposes. This is designed to be extended by a subclass with particular methods overridden by the subclass to
 * do monitoring, logging, or to do some special hackery.
 * 
 * <p>
 * See the {@link DatabaseConnectionProxyFactory} javadocs for more details.
 * </p>
 * 
 * @author graywatson
 */
public class DatabaseConnectionProxy implements DatabaseConnection {

	private final DatabaseConnection proxy;

	public DatabaseConnectionProxy(DatabaseConnection proxy) {
		this.proxy = proxy;
	}

	public boolean isAutoCommitSupported() throws SQLException {
		if (proxy == null) {
			return false;
		} else {
			return proxy.isAutoCommitSupported();
		}
	}

	public boolean isAutoCommit() throws SQLException {
		if (proxy == null) {
			return false;
		} else {
			return proxy.isAutoCommit();
		}
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if (proxy != null) {
			proxy.setAutoCommit(autoCommit);
		}
	}

	public Savepoint setSavePoint(String name) throws SQLException {
		if (proxy == null) {
			return null;
		} else {
			return proxy.setSavePoint(name);
		}
	}

	public void commit(Savepoint savePoint) throws SQLException {
		if (proxy != null) {
			proxy.commit(savePoint);
		}
	}

	public void rollback(Savepoint savePoint) throws SQLException {
		if (proxy != null) {
			proxy.rollback(savePoint);
		}
	}

	public int executeStatement(String statementStr, int resultFlags) throws SQLException {
		if (proxy == null) {
			return 0;
		} else {
			return proxy.executeStatement(statementStr, resultFlags);
		}
	}

	public CompiledStatement compileStatement(String statement, StatementType type, FieldType[] argFieldTypes,
			int resultFlags) throws SQLException {
		if (proxy == null) {
			return null;
		} else {
			return proxy.compileStatement(statement, type, argFieldTypes, resultFlags);
		}
	}

	public int insert(String statement, Object[] args, FieldType[] argfieldTypes, GeneratedKeyHolder keyHolder)
			throws SQLException {
		if (proxy == null) {
			return 0;
		} else {
			return proxy.insert(statement, args, argfieldTypes, keyHolder);
		}
	}

	public int update(String statement, Object[] args, FieldType[] argfieldTypes) throws SQLException {
		if (proxy == null) {
			return 0;
		} else {
			return proxy.update(statement, args, argfieldTypes);
		}
	}

	public int delete(String statement, Object[] args, FieldType[] argfieldTypes) throws SQLException {
		if (proxy == null) {
			return 0;
		} else {
			return proxy.delete(statement, args, argfieldTypes);
		}
	}

	public <T> Object queryForOne(String statement, Object[] args, FieldType[] argfieldTypes,
			GenericRowMapper<T> rowMapper, ObjectCache objectCache) throws SQLException {
		if (proxy == null) {
			return null;
		} else {
			return proxy.queryForOne(statement, args, argfieldTypes, rowMapper, objectCache);
		}
	}

	public long queryForLong(String statement) throws SQLException {
		if (proxy == null) {
			return 0;
		} else {
			return proxy.queryForLong(statement);
		}
	}

	public long queryForLong(String statement, Object[] args, FieldType[] argFieldTypes) throws SQLException {
		if (proxy == null) {
			return 0;
		} else {
			return proxy.queryForLong(statement, args, argFieldTypes);
		}
	}

	public void close() throws IOException {
		if (proxy != null) {
			proxy.close();
		}
	}

	public void closeQuietly() {
		if (proxy != null) {
			proxy.closeQuietly();
		}
	}

	public boolean isClosed() throws SQLException {
		if (proxy == null) {
			return true;
		} else {
			return proxy.isClosed();
		}
	}

	public boolean isTableExists(String tableName) throws SQLException {
		if (proxy == null) {
			return false;
		} else {
			return proxy.isTableExists(tableName);
		}
	}
}
