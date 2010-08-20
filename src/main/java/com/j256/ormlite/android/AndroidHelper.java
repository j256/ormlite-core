package com.j256.ormlite.android;

import java.sql.Types;
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
        typeMap.put(Types.BIT, SqlLiteType.Short);
        typeMap.put(Types.TINYINT, SqlLiteType.Short);
        typeMap.put(Types.SMALLINT, SqlLiteType.Short);
        typeMap.put(Types.INTEGER, SqlLiteType.Integer);
        typeMap.put(Types.BIGINT, SqlLiteType.Long);
        typeMap.put(Types.FLOAT, SqlLiteType.Float);
        typeMap.put(Types.REAL, SqlLiteType.Double);
        typeMap.put(Types.DOUBLE, SqlLiteType.Double);
        typeMap.put(Types.NUMERIC, SqlLiteType.Double);
        typeMap.put(Types.DECIMAL, SqlLiteType.Double);
        typeMap.put(Types.CHAR, SqlLiteType.Text);
        typeMap.put(Types.VARCHAR, SqlLiteType.Text);
        typeMap.put(Types.LONGVARCHAR, SqlLiteType.Text);
        typeMap.put(Types.DATE, SqlLiteType.Date);
        typeMap.put(Types.TIME, SqlLiteType.Date);
        typeMap.put(Types.TIMESTAMP, SqlLiteType.Date);
    }

    public static SqlLiteType getType(int jdbcFieldType)
    {
        return typeMap.get(jdbcFieldType);
    }
    
    public static int jdbcToAndroid(int columnIndex)
    {
        return columnIndex - 1;
    }

    public static int androidToJdbc(int columnIndex)
    {
        return columnIndex + 1;
    }
}
