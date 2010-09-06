package com.j256.ormlite.android.apptools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.InitConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Sep 2, 2010
 * Time: 9:39:01 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class OrmLiteSQLiteOpenHelper extends SQLiteOpenHelper
{
    AndroidConnectionSource connectionSource;

    public OrmLiteSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
        connectionSource = new AndroidConnectionSource(this);
    }

    public ConnectionSource getConnectionSource()
    {
        return connectionSource;
    }

    public abstract void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource);

    public abstract void onUpdate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion);

    @Override
    public final void onCreate(SQLiteDatabase db)
    {
        onCreate(db, new InitConnectionSource(db));
    }

    @Override
    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpdate(db, new InitConnectionSource(db), oldVersion, newVersion);
    }
}
