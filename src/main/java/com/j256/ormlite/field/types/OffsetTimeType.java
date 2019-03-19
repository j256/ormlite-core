package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * A custom persister that is able to store the java.time.OffsetTime class in the database as Time With Time Zone object.
 * This class does not have a SQL backup counter-part, the database should support JDBC 4.2 for it to be used.
 *
 * @author graynk
 */
public class OffsetTimeType extends BaseLocalDateType {

    private static final OffsetTimeType singleton = isJavaTimeSupported() ? new OffsetTimeType() : null;
    public static OffsetTimeType getSingleton() { return singleton; }
    private OffsetTimeType() { super(SqlType.OFFSET_TIME, new Class<?>[] { OffsetTime.class }); }
    protected OffsetTimeType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }
    protected OffsetTimeType(SqlType sqlType) { super(sqlType); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return OffsetTime.parse(defaultStr, new DateTimeFormatterBuilder()
                    .appendPattern("HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .appendPattern("x")
                    .toFormatter());
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
