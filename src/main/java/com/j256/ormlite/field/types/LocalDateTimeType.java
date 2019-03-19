package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * A custom persister that is able to store the java.time.LocalDateTime class in the database as Timestamp object.
 *
 * @author graynk
 */
public class LocalDateTimeType extends BaseLocalDateType {

    private static final LocalDateTimeType singleton = isJavaTimeSupported() ? new LocalDateTimeType() : null;
    public static LocalDateTimeType getSingleton() { return singleton; }
    private LocalDateTimeType() { super(SqlType.LOCAL_DATE_TIME, new Class<?>[] { LocalDateTime.class }); }
    protected LocalDateTimeType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }
    protected LocalDateTimeType(SqlType sqlType) { super(sqlType); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return LocalDateTime.parse(defaultStr, new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .toFormatter());
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
    public Object moveToNextValue(Object currentValue) {
        LocalDateTime datetime = (LocalDateTime) currentValue;
        return datetime.plusNanos(1);
    }

    @Override
    public boolean isValidForField(Field field) {
        return (field.getType() == LocalDateTime.class);
    }
}
