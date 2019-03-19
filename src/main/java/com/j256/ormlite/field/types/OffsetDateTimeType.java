package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * A custom persister that is able to store the java.time.OffsetDateTime class in the database as Timestamp with Time Zone object.
 * This class does not have a SQL backup counter-part, the database should support JDBC 4.2 for it to be used.
 *
 * @author graynk
 */
public class OffsetDateTimeType extends BaseLocalDateType {

    private static final OffsetDateTimeType singleton = isJavaTimeSupported() ? new OffsetDateTimeType() : null;
    public static OffsetDateTimeType getSingleton() { return singleton; }
    private OffsetDateTimeType() { super(SqlType.OFFSET_DATE_TIME, new Class<?>[] { OffsetDateTime.class }); }
    protected OffsetDateTimeType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return OffsetDateTime.parse(defaultStr, new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .appendPattern("x")
                    .toFormatter());
        } catch (NumberFormatException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType +
                    " parsing default OffsetDateTime value: " + defaultStr, e);
        }
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getOffsetDateTime(columnPos);
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
}
