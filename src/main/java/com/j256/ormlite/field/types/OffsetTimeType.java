package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;
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
public class OffsetTimeType extends BaseLocalDateType {

    private static OffsetTimeType singleton;
    public static OffsetTimeType getSingleton() {
        if (singleton == null) {
            try {
                Class.forName("java.time.OffsetTime", false, null);
                singleton = new OffsetTimeType();
            } catch (ClassNotFoundException e) {
                return null; // No java.time on classpath;
            }
        }
        return singleton;
    }
    private OffsetTimeType() { super(SqlType.OFFSET_TIME, new Class<?>[] { OffsetTime.class }); }
    protected OffsetTimeType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return OffsetTime.parse(defaultStr, DateTimeFormatter.ofPattern("HH:mm:ss[.SSSSSS]x"));
        } catch (NumberFormatException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType +
                    " parsing default OffsetTime value: " + defaultStr, e);
        }
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getOffsetTime(columnPos);
    }

    @Override
    public Object moveToNextValue(Object currentValue) {
        OffsetTime time = (OffsetTime) currentValue;
        return time.plusNanos(1);
    }

    @Override
    public boolean isValidForField(Field field) {
        return (field.getType() == OffsetTime.class);
    }
}
