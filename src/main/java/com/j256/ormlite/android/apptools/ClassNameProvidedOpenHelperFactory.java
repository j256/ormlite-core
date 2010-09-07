package com.j256.ormlite.android.apptools;

import android.content.Context;

import java.lang.reflect.Constructor;

/**
 * The default helper factory.  Provide 'open_helper_classname' in the @string resource.  
 *
 * @author kevingalligan, graywatson
 */
public class ClassNameProvidedOpenHelperFactory implements AndroidSQLiteManager.SQLiteOpenHelperFactory {
    public OrmLiteSQLiteOpenHelper createHelper(Context c) {
        int id = c.getResources().getIdentifier("open_helper_classname", "string", c.getPackageName());
        if(id == 0)
            throw new IllegalStateException("string resrouce open_helper_classname required");

        String className = c.getResources().getString(id);
        try {
            Class<?> helperClass = Class.forName(className);
            Constructor<?> constructor = helperClass.getConstructor(Context.class);
            return (OrmLiteSQLiteOpenHelper) constructor.newInstance(c);
        } catch (Exception e) {
            throw new IllegalStateException("Count not create helper instance", e);
        }
    }
}
