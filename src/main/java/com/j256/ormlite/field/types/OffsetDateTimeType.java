package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.OffsetDateTime;
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
public class OffsetDateTimeType extends BaseDataType {

    private static OffsetDateTimeType singleton;
    public static OffsetDateTimeType getSingleton() {
        if (singleton == null) {
            try {
                Class.forName("java.time.OffsetDateTime", false, null);
                singleton = new OffsetDateTimeType();
            } catch (ClassNotFoundException e) {
                return null; // No java.time on classpath;
            }
        }
        return singleton;
    }
    private OffsetDateTimeType() { super(SqlType.OFFSET_DATE_TIME, new Class<?>[] { OffsetDateTime.class }); }
    protected OffsetDateTimeType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return OffsetDateTime.parse(defaultStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]x"));
        } catch (NumberFormatException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType +
                    " parsing default LocalDateTime value: " + defaultStr, e);
        }
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getOffsetDateTime(columnPos);
    }

    @Override
    public boolean isValidForVersion() {
        return true;
    }

    @Override
    public Object moveToNextValue(Object currentValue) {
        OffsetDateTime datetime = (OffsetDateTime) currentValue;
        return datetime.plusNanos(1);
    }

    @Override
    public boolean isValidForField(Field field) {
        return (field.getType() == OffsetDateTime.class);
    }

    @Override
    public boolean isArgumentHolderRequired() {
        return true;
    }
}
