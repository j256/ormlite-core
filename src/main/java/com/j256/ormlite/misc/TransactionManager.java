package com.j256.ormlite.misc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;

/**
 * Wrapper around the Spring Framework's {@link DataSourceTransactionManager} providing basic transaction support for a
 * particular {@link DataSource}.
 * 
 * <p>
 * <b>NOTE:</b> For transactions to work, the database being used must support the functionality.
 * </p>
 * 
 * <p>
 * <b> NOTE: </b> If you are using the Spring type wiring in Java, {@link #initialize} should be called after all of the
 * set methods. In Spring XML, init-method="initialize" should be used.
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * TransactionManager transactionMgr = new TransactionManager(dataSource);
 * ...
 * mgr.callInTransaction(new Callable&lt;Void&gt;() {
 * 		public Void call() throws Exception {
 * 			// delete both objects but make sure that if either one fails, the transaction is rolled back
 * 			// and both objects are "restored" to the database
 * 			fooDao.delete(foo);
 * 			barDao.delete(bar);
 * 			return null;
 * 		}
 * });
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * For Spring wiring of a Transaction Manager bean, we would do something like the following:
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * &lt;bean id="transactionManager" class="com.j256.ormlite.misc.TransactionManager" init-method="initialize"&gt;
 * 	&lt;property name="dataSource" ref="dataSource" /&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * </blockquote>
 * 
 * @author graywatson
 */
public class TransactionManager {

	private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
	private static final String SAVE_POINT_PREFIX = "ORMLITE";

	private DataSource dataSource;
	private Boolean savePointsSupported;
	private static AtomicInteger savePointCounter = new AtomicInteger();

	/**
	 * Constructor for Spring type wiring if you are using the set methods.
	 */
	public TransactionManager() {
		// for spring wiring -- must call setDataSource()
	}

	/**
	 * Constructor for direct java code wiring.
	 */
	public TransactionManager(DataSource dataSource) {
		this.dataSource = dataSource;
		initialize();
	}

	/**
	 * If you are using the Spring type wiring, this should be called after all of the set methods.
	 */
	public void initialize() {
		if (dataSource == null) {
			throw new IllegalStateException("dataSource was not set on " + getClass().getSimpleName());
		}
	}

	/**
	 * Execute the {@link Callable} class inside of a transaction. If the callable returns then the transaction is
	 * committed. If the callable throws an exception then the transaction is rolled back and a {@link SQLException} is
	 * thrown by this method.
	 * 
	 * <p>
	 * <b> NOTE: </b> If your callable block really doesn't have a return object then use the Void class and return null
	 * from the call method.
	 * </p>
	 * 
	 * @param callable
	 *            Callable to execute inside of the transaction.
	 * @return The object returned by the callable.
	 * @throws SQLException
	 *             If the callable threw an exception then the transaction is rolled back and a SQLException wraps the
	 *             callable exception and is thrown by this method.
	 */
	public <T> T callInTransaction(final Callable<T> callable) throws SQLException {
		Connection connection = dataSource.getConnection();
		boolean autoCommitAtStart = false;
		try {
			if (savePointsSupported == null) {
				DatabaseMetaData metaData = connection.getMetaData();
				savePointsSupported = metaData.supportsSavepoints();
				logger.debug("transactions {} supported by connection", (savePointsSupported ? "are" : "are not"));
			}
			// change from auto-commit mode
			autoCommitAtStart = connection.getAutoCommit();
			if (autoCommitAtStart) {
				connection.setAutoCommit(false);
				logger.debug("had to set auto-commit to false");
			}
			Savepoint savePoint;
			if (savePointsSupported) {
				savePoint = connection.setSavepoint(SAVE_POINT_PREFIX + savePointCounter.incrementAndGet());
				logger.debug("started savePoint transaction {}", savePoint.getSavepointName());
			} else {
				savePoint = null;
			}
			try {
				T result = callable.call();
				releaseSavePoint(connection, savePoint);
				return result;
			} catch (SQLException e) {
				rollBackSavePoint(connection, savePoint);
				throw e;
			} catch (Exception e) {
				rollBackSavePoint(connection, savePoint);
				throw SqlExceptionUtil.create("Operation in transaction threw non-SQL exception", e);
			}
		} finally {
			if (autoCommitAtStart) {
				// try to restore if we are in auto-commit mode
				connection.setAutoCommit(true);
				logger.debug("restored auto-commit to true");
			}
		}
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private void releaseSavePoint(Connection connection, Savepoint savePoint) throws SQLException {
		if (savePoint == null) {
			connection.commit();
			logger.debug("committed transaction");
		} else {
			String name = savePoint.getSavepointName();
			connection.releaseSavepoint(savePoint);
			logger.debug("released savePoint transaction {}", name);
		}
	}

	private void rollBackSavePoint(Connection connection, Savepoint savePoint) throws SQLException {
		if (savePoint == null) {
			connection.rollback();
			logger.debug("rolled back transaction");
		} else {
			String name = savePoint.getSavepointName();
			connection.rollback(savePoint);
			logger.debug("rolled back savePoint transaction {}", name);
		}
	}
}
