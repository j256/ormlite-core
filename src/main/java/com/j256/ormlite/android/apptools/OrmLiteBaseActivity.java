package com.j256.ormlite.android.apptools;

import android.app.Activity;
import android.content.Context;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Base class to use for activities in Android.
 * 
 * If you are using the default helper factory, you can simply call {@link #getHelper()} to get your helper class, or
 * {@link #getConnectionSource()} to get a {@link ConnectionSource}.
 * 
 * The method {@link #getHelper()} assumes you are using the default helper factory -- see {@link AndroidSqliteManager}.
 * If not, you'll need to provide your own helper instances which will need to implement a reference counting scheme.
 * This method will only be called if you use the database, and only called once for this activity's life-cycle. 'close'
 * will also be called once for each call to createInstance.
 * 
 * @author kevingalligan
 */
public abstract class OrmLiteBaseActivity extends Activity {

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
