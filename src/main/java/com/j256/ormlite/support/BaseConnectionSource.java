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
	private ThreadLocal<NestedConnection> specialConnection = new ThreadLocal<NestedConnection>();

	/**
	 * Returns the connection that has been saved or null if none.
	 */
	protected DatabaseConnection getSavedConnection() throws SQLException {
		if (!usedSpecialConnection) {
			return null;
		}
		NestedConnection nested = specialConnection.get();
		if (nested == null) {
			return null;
		} else {
			return nested.connection;
		}
	}

	/**
	 * Return true if the connection being released is the one that has been saved.
	 */
	protected boolean isSavedConnection(DatabaseConnection connection) throws SQLException {
		if (!usedSpecialConnection) {
			return false;
		}
		NestedConnection currentSaved = specialConnection.get();
		if (currentSaved == null) {
			return false;
		} else if (currentSaved.connection == connection) {
			// ignore the release when we have a saved connection
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Save this connection as our special connection to be returned by the {@link #getSavedConnection()} method.
	 * 
	 * @return True if the connection was saved or false if it was already saved.
	 */
	protected boolean saveSpecial(DatabaseConnection connection) {
		// check for a connection already saved
		NestedConnection currentSaved = specialConnection.get();
		if (currentSaved == null) {
			/*
			 * This is fine to not be synchronized since it is only this thread we care about. Other threads will set
			 * this or have it synchronized in over time.
			 */
			usedSpecialConnection = true;
			specialConnection.set(new NestedConnection(connection));
			return true;
		} else {
			if (currentSaved.connection != connection) {
				throw new IllegalStateException("trying to save connection " + connection
						+ " but already have saved connection " + currentSaved.connection);
			}
			// we must have a save call within another save
			currentSaved.increment();
			return false;
		}
	}

	/**
	 * Clear the connection that was previoused saved.
	 */
	protected void clearSpecial(DatabaseConnection connection, Logger logger) {
		NestedConnection currentSaved = specialConnection.get();
		if (currentSaved == null) {
			logger.error("no connection has been saved when clear() called");
		} else {
			if (currentSaved.connection != connection) {
				logger.error("connection saved {} is not the one being cleared {}", currentSaved.connection, connection);
			} else if (currentSaved.decrementAndGet() == 0) {
				// we only clear the connection if nested counter is 0
				specialConnection.set(null);
			}
		}
		// release should then be called after clear
	}

	private class NestedConnection {
		public final DatabaseConnection connection;
		private int nestedC;

		public NestedConnection(DatabaseConnection connection) {
			this.connection = connection;
			this.nestedC = 1;
		}

		public void increment() {
			nestedC++;
		}

		public int decrementAndGet() {
			nestedC--;
			return nestedC;
		}
	}
}
