package com.j256.ormlite.field.types;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * A custom persister that is able to store the java.time.OffsetTime class in the database as Timestamp With Time Zone object.
 * This class should be used only when database used does not support Time With Time Zone, since it converts java.time.OffsetTime
 * to java.time.OffsetDateTime, fixing date part at epoch, to be stored as Timestamp With Time Zone.
 *
 * @author graynk
 */
public class OffsetTimeCompatType extends OffsetTimeType {

    private static final OffsetTimeCompatType singleton = isJavaTimeSupported() ? new OffsetTimeCompatType() : null;
    public static OffsetTimeCompatType getSingleton() { return singleton; }
    private OffsetTimeCompatType() { super(SqlType.OFFSET_DATE_TIME); }
    protected OffsetTimeCompatType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            OffsetTime time = OffsetTime.parse(defaultStr, new DateTimeFormatterBuilder()
                    .appendPattern("[yyyy-MM-dd ]HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .appendPattern("x")
                    .toFormatter());
            return javaToSqlArg(fieldType, time);
        } catch (NumberFormatException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType +
                    " parsing default OffsetTime value: " + defaultStr, e);
        }
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getOffsetDateTime(columnPos);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        OffsetDateTime value = (OffsetDateTime) sqlArg;
        return value.toOffsetTime();
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        OffsetTime time = (OffsetTime) javaObject;
        return time.atDate(LocalDate.ofEpochDay(0));
    }
}
