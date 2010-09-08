package com.j256.ormlite.android.apptools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.InitConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Sqlite database open helper which can be extended by your application.
 * 
 * @author kevingalligan
 */
public abstract class OrmLiteSqliteOpenHelper extends SQLiteOpenHelper {

	AndroidConnectionSource connectionSource;

	public OrmLiteSqliteOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		connectionSource = new AndroidConnectionSource(this);
	}

	public ConnectionSource getConnectionSource() {
		return connectionSource;
	}

	/**
	 * What to do when your database needs to be created. Usually this entails creating the tables and loading any
	 * initial data.
	 */
	public abstract void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource);

	/**
	 * What to do when your database needs to be updated. This could mean careful migration of old data to new data.
	 * Maybe adding or deleting database columns, etc..
	 */
	public abstract void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion,
			int newVersion);

	@Override
	public final void onCreate(SQLiteDatabase db) {
		onCreate(db, new InitConnectionSource(db));
	}

	@Override
	public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, new InitConnectionSource(db), oldVersion, newVersion);
	}
}
