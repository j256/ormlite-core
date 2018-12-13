package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
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
public class LocalDateSqlType extends LocalDateType {

    private static LocalDateSqlType singleton;
    public static LocalDateSqlType getSingleton() {
        if (singleton == null) {
            try {
                Class.forName("java.time.LocalDate", false, null);
                singleton = new LocalDateSqlType();
            } catch (ClassNotFoundException e) {
                return null; // No java.time on classpath;
            }
        }
        return singleton;
    }
    private LocalDateSqlType() { super(SqlType.LOCAL_DATE, new Class<?>[] { LocalDate.class }); }
    protected LocalDateSqlType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return Date.valueOf(LocalDate.parse(defaultStr, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (NumberFormatException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType +
                    " parsing default LocalDate value: " + defaultStr, e);
        }
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getDate(columnPos);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        LocalDate date = (LocalDate) javaObject;
        return Date.valueOf(date);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        Date date = (Date) sqlArg;
        return date.toLocalDate();
    }
}
