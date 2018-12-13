package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
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
 *
 * @author graynk
 */
public class LocalTimeSqlType extends LocalTimeType {

    private static LocalTimeSqlType singleton;
    public static LocalTimeSqlType getSingleton() {
        if (singleton == null) {
            try {
                Class.forName("java.time.LocalTime", false, null);
                singleton = new LocalTimeSqlType();
            } catch (ClassNotFoundException e) {
                return null; // No java.time on classpath;
            }
        }
        return singleton;
    }
    private LocalTimeSqlType() { super(SqlType.LOCAL_TIME); }
    protected LocalTimeSqlType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return Time.valueOf(LocalTime.parse(defaultStr, DateTimeFormatter.ofPattern("HH:mm:ss[.SSSSSS]")));
        } catch (NumberFormatException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType +
                    " parsing default LocalTime value: " + defaultStr, e);
        }
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
