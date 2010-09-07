package com.j256.ormlite.android.apptools;

import android.app.Activity;
import android.content.Context;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Base class to use for activities in Android.
 *
 * If you are using the default helper factory, you can simply call 'getHelper' to get your helper class, or 'getConnectionSource' to get an ormlite
 * ConnectionSource instance.
 *
 * The method createInstance assumes you are using the default helper factory.  If not, you'll need to provide your own helper instances.  This should
 * return a new instance, or you'll need to implement
 * a reference counting scheme.  This method will only be called if you use the database, and only called once for this activity's
 * lifecycle.  'close' will also be called once for each call to createInstance.
 *
 * @author kevingalligan, graywatson
 */
public abstract class OrmLiteBaseActivity extends Activity
{
    private OrmLiteSQLiteOpenHelper helper;

    public OrmLiteSQLiteOpenHelper createInstance(Context context)
    {
        return AndroidSQLiteManager.getInstance(context);
    }

    public synchronized OrmLiteSQLiteOpenHelper getHelper()
    {
        if(helper == null)
        {
            helper = createInstance(this);
        }
        return helper;
    }

    public ConnectionSource getConnectionSource()
    {
        return getHelper().getConnectionSource();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(helper != null)
            helper.close();
    }
}
