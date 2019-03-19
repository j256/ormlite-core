package com.j256.ormlite.field.types;

import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * A custom persister that is able to store the java.time.LocalTime class in the database as Time object.
 * This class should be used only when database used does not support JDBC 4.2, since it converts java.time.LocalTime
 * to java.sql.Time.
 *
 * @author graynk
 */
public class LocalTimeSqlType extends LocalTimeType {

    private static final LocalTimeSqlType singleton = isJavaTimeSupported() ? new LocalTimeSqlType() : null;
    public static LocalTimeSqlType getSingleton() { return singleton; }
    private LocalTimeSqlType() { super(SqlType.LOCAL_TIME); }
    protected LocalTimeSqlType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        return Time.valueOf((LocalTime) super.parseDefaultString(fieldType, defaultStr));
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getTime(columnPos);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        Time value = (Time) sqlArg;
        return value.toLocalTime();
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        LocalTime time = (LocalTime) javaObject;
        return Time.valueOf(time);
    }
}
