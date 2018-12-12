package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
public class LocalDateTimeType extends BaseDataType {

    private static LocalDateTimeType singleton;
    public static LocalDateTimeType getSingleton() {
        if (singleton == null) {
            try {
                Class.forName("java.time.LocalDateTime", false, null);
                singleton = new LocalDateTimeType();
            } catch (ClassNotFoundException e) {
                return null; // No java.time on classpath;
            }
        }
        return singleton;
    }
    private LocalDateTimeType() { super(SqlType.LOCAL_DATE_TIME, new Class<?>[] { LocalDateTime.class }); }
    protected LocalDateTimeType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return LocalDateTime.parse(defaultStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]"));
        } catch (NumberFormatException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType +
                    " parsing default LocalDateTime value: " + defaultStr, e);
        }
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getLocalDateTime(columnPos);
    }

    @Override
    public boolean isValidForVersion() {
        return true;
    }

    @Override
    public Object moveToNextValue(Object currentValue) {
        LocalDateTime datetime = (LocalDateTime) currentValue;
        return datetime.plusNanos(1);
    }

    @Override
    public boolean isValidForField(Field field) {
        return (field.getType() == LocalDateTime.class);
    }

    @Override
    public boolean isArgumentHolderRequired() {
        return true;
    }
}
