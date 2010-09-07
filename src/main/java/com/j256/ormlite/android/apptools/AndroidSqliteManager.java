package com.j256.ormlite.android.apptools;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

/**
 * There are several schemes to manage the database connections in an Android app, but as an app gets more complicated,
 * there are many potential places where database locks can occur. This class helps organize database creation and
 * access in a manner that will allow database connection sharing between multiple processes in a single app.
 * 
 * To use this class, you must either call init with an instance of SQLiteOpenHelperFactory, or (more commonly) provide
 * the name of your helper class in the Android resource "@string" under "open_helper_classname". The factory simply
 * creates your SQLiteOpenHelper instance. This will only be called once per app VM instance and kept in a static field.
 * 
 * The SQLiteOpenHelper and database classes maintain one connection under the hood, and prevent locks in the java code.
 * Creating multiple connections can potentially be a source of trouble. This class shares the same connection instance
 * between multiple clients, which will allow multiple activities and services to run at the same time.
 * 
 * @author kevingalligan
 */
public class AndroidSqliteManager {

	private static SqliteOpenHelperFactory factory;
	private static volatile OrmLiteSqliteOpenHelper instance;
	private static AtomicInteger instanceCount = new AtomicInteger(0);

	/**
	 * Initialize the manager with your own helper factory. Default is to use the
	 * {@link ClassNameProvidedOpenHelperFactory}.
	 */
	public static void init(SqliteOpenHelperFactory factory) {
		AndroidSqliteManager.factory = factory;
	}

	/**
	 * Get the static instance of our open helper. This has a usage counter on it so make sure all calls to this method
	 * have an associated call to {@link #release()}.
	 */
	public static OrmLiteSqliteOpenHelper getHelper(Context context) {
		if (factory == null) {
			ClassNameProvidedOpenHelperFactory fact = new ClassNameProvidedOpenHelperFactory();
			init(fact);
		}

		if (instance == null) {
			synchronized (AndroidSqliteManager.class) {
				/*
				 * Double-check locking OK due to 'volatile'. Just saying...
				 * http://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
				 */
				if (instance == null) {
					instance = factory.getHelper(context);
				}
			}
		}

		instanceCount.incrementAndGet();
		return instance;
	}

	/**
	 * Release the helper that was previous returned by a call to {@link #getHelper(Context)}. This will decrement the
	 * usage counter and close the helper if the counter is 0.
	 */
	public static void release() {
		int val = instanceCount.decrementAndGet();
		if (val == 0) {
			synchronized (AndroidSqliteManager.class) {
				if (instance != null) {
					instance.close();
					instance = null;
				}
			}
		} else if (val < 0) {
			throw new IllegalStateException("Too many calls to close");
		}
	}

	/**
	 * Factory for providing open helpers.
	 */
	public interface SqliteOpenHelperFactory {

		/**
		 * Create and return an open helper associated with the context.
		 */
		public OrmLiteSqliteOpenHelper getHelper(Context c);
	}
}
