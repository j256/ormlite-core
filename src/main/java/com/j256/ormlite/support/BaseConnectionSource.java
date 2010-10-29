package com.j256.ormlite.support;

import java.sql.SQLException;

import com.j256.ormlite.logger.Logger;

/**
 * Connection source base class which provides the save/clear mechanism using a thread local.
 * 
 * @author graywatson
 */
public abstract class BaseConnectionSource {

	protected boolean usedSpecialConnection = false;
	private ThreadLocal<DatabaseConnection> specialConnection = new ThreadLocal<DatabaseConnection>();

	/**
	 * Returns the connection that has been saved or null if none.
	 */
	protected DatabaseConnection getSavedConnection() throws SQLException {
		if (usedSpecialConnection) {
			return specialConnection.get();
		} else {
			return null;
		}
	}

	/**
	 * Return true if the connection being released is the one that has been saved.
	 */
	protected boolean isSavedConnection(DatabaseConnection connection) throws SQLException {
		if (usedSpecialConnection && specialConnection.get() == connection) {
			// ignore the release when we are in a transaction
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Save this connection as our special connection to be returned by the {@link #getSavedConnection()} method.
	 */
	protected void saveSpecial(DatabaseConnection connection) {
		// check for a connection already saved
		DatabaseConnection currentSavedConn = specialConnection.get();
		if (currentSavedConn != null) {
			if (currentSavedConn == connection) {
				throw new IllegalStateException("nested transactions are not current supported");
			} else {
				throw new IllegalStateException("trying to save connection " + connection
						+ " but already have saved connection " + currentSavedConn);
			}
		}
		/*
		 * This is fine to not be synchronized since it is only this thread we care about. Other threads will set this
		 * or have it synchronized in over time.
		 */
		usedSpecialConnection = true;
		specialConnection.set(connection);
	}

	/**
	 * Clear the connection that was previoused saved.
	 */
	protected void clearSpecial(DatabaseConnection connection, Logger logger) {
		DatabaseConnection currentSavedConn = specialConnection.get();
		if (currentSavedConn == null) {
			logger.error("no transaction has been saved when clear() called");
		} else {
			if (currentSavedConn != connection) {
				logger.error("transaction saved {} is not the one being cleared {}", currentSavedConn, connection);
			}
			specialConnection.set(null);
		}
		// release should then be called after clear
	}
}
