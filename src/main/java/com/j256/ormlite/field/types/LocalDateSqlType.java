package com.j256.ormlite.field.types;

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
 * This class should be used only when database used does not support JDBC 4.2, since it converts java.time.LocalDate
 * to java.sql.Date.
 *
 * @author graynk
 */
public class LocalDateSqlType extends LocalDateType {

    private static LocalDateSqlType singleton = isJavaTimeSupported() ? new LocalDateSqlType() : null;
    public static LocalDateSqlType getSingleton() { return singleton; }
    private LocalDateSqlType() { super(SqlType.LOCAL_DATE); }
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
