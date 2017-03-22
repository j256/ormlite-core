package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.table.TableInfo;

/**
 * Assists in building sql DELETE statements for a particular table in a particular database.
 * 
 * @param <T>
 *            The class that the code will be operating on.
 * @param <ID>
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class DeleteBuilder<T, ID> extends StatementBuilder<T, ID> {

	private String tableSchema;

	// NOTE: any fields here should be added to the clear() method below

	public DeleteBuilder(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao) {
		super(databaseType, tableInfo, dao, StatementType.DELETE);
	}

	public DeleteBuilder(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao, String schema) {
		super(databaseType, tableInfo, dao, StatementType.DELETE);
		this.tableSchema = schema;
	}

	
	/**
	 * Build and return a prepared delete that can be used by {@link Dao#delete(PreparedDelete)} method. If you change
	 * the where or make other calls you will need to re-call this method to re-prepare the statement for execution.
	 */
	public PreparedDelete<T> prepare() throws SQLException {
		return super.prepareStatement(null, false);
	}

	/**
	 * A short cut to {@link Dao#delete(PreparedDelete)}.
	 */
	public int delete() throws SQLException {
		return dao.delete(prepare());
	}

	@Override
	public void reset() {
		// NOTE: this is here because it is in the other sub-classes
		super.reset();
	}

	@Override
	protected void appendStatementStart(StringBuilder sb, List<ArgumentHolder> argList) {
		sb.append("DELETE FROM ");
		if (this.tableSchema != null){
			databaseType.appendEscapedEntityName(sb, tableSchema);
			sb.append(".");
		}
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(' ');
	}

	@Override
	protected void appendStatementEnd(StringBuilder sb, List<ArgumentHolder> argList) {
		// noop
	}
	
	/**
	 * Manually force table schema name to this query
	 */
	public void setSchema(String tableSchema) {
		this.tableSchema = tableSchema;
	}
}
