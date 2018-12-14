package com.j256.ormlite.field.types;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * A custom persister that is able to store the java.time.LocalDateTime class in the database as Timestamp object.
 * This class should be used only when database used does not support JDBC 4.2, since it converts java.time.LocalDateTime
 * to java.sql.Timestamp.
 *
 * @author graynk
 */
public class LocalDateTimeSqlType extends LocalDateTimeType {

    private static final LocalDateTimeSqlType singleton = isJavaTimeSupported() ? new LocalDateTimeSqlType() : null;
    public static LocalDateTimeSqlType getSingleton() { return singleton; }
    private LocalDateTimeSqlType() { super(SqlType.LOCAL_DATE_TIME); }
    protected LocalDateTimeSqlType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return Timestamp.valueOf(LocalDateTime.parse(defaultStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]")));
        } catch (NumberFormatException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType +
                    " parsing default LocalDateTime value: " + defaultStr, e);
        }
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getTimestamp(columnPos);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        Timestamp value = (Timestamp) sqlArg;
        return value.toLocalDateTime();
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        LocalDateTime datetime = (LocalDateTime) javaObject;
        return Timestamp.valueOf(datetime);
    }
}
