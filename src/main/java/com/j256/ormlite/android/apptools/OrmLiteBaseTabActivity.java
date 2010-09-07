package com.j256.ormlite.android.apptools;

import android.app.TabActivity;
import android.content.Context;

import com.j256.ormlite.support.ConnectionSource;

/**
 * Base class to use for Tab activities in Android.
 * 
 * For more information, see {@link OrmLiteBaseActivity}.
 * 
 * @author kevingalligan
 */
public abstract class OrmLiteBaseTabActivity extends TabActivity {

	private OrmLiteSqliteOpenHelper helper;

	public OrmLiteSqliteOpenHelper getHelper(Context context) {
		return AndroidSqliteManager.getHelper(context);
	}

	public synchronized OrmLiteSqliteOpenHelper getHelper() {
		if (helper == null) {
			helper = getHelper(this);
		}
		return helper;
	}

	public ConnectionSource getConnectionSource() {
		return getHelper().getConnectionSource();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (helper != null) {
			helper.close();
			helper = null;
		}
	}
}
