package com.j256.ormlite.android;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 9:57:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidHelper
{
    public static int jdbcToAndroid(int columnIndex)
    {
        return columnIndex - 1;
    }

    public static int androidToJdbc(int columnIndex)
    {
        return columnIndex + 1;
    }
}
