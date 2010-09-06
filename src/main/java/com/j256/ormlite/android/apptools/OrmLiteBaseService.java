package com.j256.ormlite.android.apptools;

import android.app.Service;
import android.content.Context;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Base class to use for Services in Android.
 *
 * The method createInstance should create a helper instance.  This should be a new instance, or you'll need to implement
 * a reference counting scheme.  This method will only be called if you use the database, and only called once for this service's 
 * lifecycle.  'close' will also be called once for each call to createInstance.
 *
 * If you are using @see com.j256.ormlite.android.apptools.AndroidSQLiteManager AndroidSQLiteManager,
 * simply return an instance from that.
 *
 * @author kevingalligan, graywatson
 */
public abstract class OrmLiteBaseService extends Service
{
    private OrmLiteSQLiteOpenHelper helper;

    public abstract OrmLiteSQLiteOpenHelper createInstance(Context context);

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
    public void onDestroy()
    {
        super.onDestroy();
        if(helper != null)
            helper.close();
    }
}
