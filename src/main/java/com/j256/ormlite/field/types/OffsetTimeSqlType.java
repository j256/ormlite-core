package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * A custom persister that is able to store the java.time.LocalDate class in the database as Date object.
 *
 * @author graynk
 */
public class OffsetTimeSqlType extends OffsetTimeType {

    private static OffsetTimeSqlType singleton;
    public static OffsetTimeSqlType getSingleton() {
        if (singleton == null) {
            try {
                Class.forName("java.time.OffsetTime", false, null);
                singleton = new OffsetTimeSqlType();
            } catch (ClassNotFoundException e) {
                return null; // No java.time on classpath;
            }
        }
        return singleton;
    }
    private OffsetTimeSqlType() { super(SqlType.OFFSET_DATE_TIME); }
    protected OffsetTimeSqlType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return OffsetDateTime.parse(defaultStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]x"));
        } catch (NumberFormatException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType +
                    " parsing default OffsetTime value: " + defaultStr, e);
        }
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getOffsetDateTime(columnPos);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        OffsetDateTime value = (OffsetDateTime) sqlArg;
        return value.toOffsetTime();
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        OffsetTime time = (OffsetTime) javaObject;
        return time.atDate(LocalDate.ofEpochDay(0));
    }
}
