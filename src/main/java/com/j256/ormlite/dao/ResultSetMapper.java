package com.j256.ormlite.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public interface ResultSetMapper<T> {
    /**
     * Map the row the result set is at when passed in. This method should not cause the result
     * set to advance, allow jDBI to do that, please.
     *
     * @param r the result set being iterated
     * @return the value to return for this row
     * @throws java.sql.SQLException if anythign goes wrong go ahead and let this percolate, jDBI will handle it
     */
    public T mapRow(ResultSet r) throws SQLException;
}
