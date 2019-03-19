package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * A custom persister that is able to store the java.time.LocalTime class in the database as Time object.
 *
 * @author graynk
 */
public class LocalTimeType extends BaseLocalDateType {

    private static final LocalTimeType singleton = isJavaTimeSupported() ? new LocalTimeType() : null;
    public static LocalTimeType getSingleton() { return singleton; }
    private LocalTimeType() { super(SqlType.LOCAL_TIME, new Class<?>[] { LocalTime.class }); }
    protected LocalTimeType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }
    protected LocalTimeType(SqlType sqlType) { super(sqlType); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        return LocalTime.parse(defaultStr, new DateTimeFormatterBuilder()
                .appendPattern("HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .toFormatter());
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getLocalTime(columnPos);
    }

    @Override
    public Object moveToNextValue(Object currentValue) {
        LocalTime time = (LocalTime) currentValue;
        return time.plusNanos(1);
    }

    @Override
    public boolean isValidForField(Field field) {
        return (field.getType() == LocalTime.class);
    }
}
