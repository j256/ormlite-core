package com.j256.ormlite.android;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 9:57:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidHelper
{
    public enum SqlLiteType
    {
        Short, Integer, Long, Float, Double, Text, Date
    }

	private static final Map<Integer, SqlLiteType> typeMap = new HashMap<Integer, SqlLiteType>();

    static {
        typeMap.put(-7, SqlLiteType.Short);    //        BIT = -7;
        typeMap.put(-6, SqlLiteType.Short);    //        TINYINT = -6;
        typeMap.put(5, SqlLiteType.Short);    //        SMALLINT = 5;
        typeMap.put(4, SqlLiteType.Integer);    //        INTEGER = 4;
        typeMap.put(-5, SqlLiteType.Long);    //        BIGINT = -5;
        typeMap.put(6, SqlLiteType.Float);    //        FLOAT = 6;
        typeMap.put(7, SqlLiteType.Double);    //        REAL = 7;
        typeMap.put(8, SqlLiteType.Double);    //        DOUBLE = 8;
        typeMap.put(2, SqlLiteType.Double);    //        NUMERIC = 2;
        typeMap.put(3, SqlLiteType.Double);    //        DECIMAL = 3;
        typeMap.put(1, SqlLiteType.Text);    //        CHAR = 1;
        typeMap.put(12, SqlLiteType.Text);    //        VARCHAR = 12;
        typeMap.put(-1, SqlLiteType.Text);    //        LONGVARCHAR = -1;
        typeMap.put(91, SqlLiteType.Date);    //        DATE = 91;
        typeMap.put(92, SqlLiteType.Date);    //        TIME = 92;
        typeMap.put(93, SqlLiteType.Date);    //        TIMESTAMP = 93;
            //        BINARY = -2;
            //        VARBINARY = -3;
            //        LONGVARBINARY = -4;
            //        NULL = 0;
    }

    public static SqlLiteType getType(int jdbcFieldType)
    {
        return typeMap.get(jdbcFieldType);
    }
    
    public static int jdbcToAndroid(int i)
    {
        return i-1;
    }

    public static int androidToJdbc(int i)
    {
        return i+1;
    }
}
