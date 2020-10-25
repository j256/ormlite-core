package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDate;
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
public class LocalDateType extends BaseLocalDateType {

    private static final LocalDateType singleton = isJavaTimeSupported() ? new LocalDateType() : null;
    public static LocalDateType getSingleton() { return singleton; }
    private LocalDateType() { super(SqlType.LOCAL_DATE, new Class<?>[] { LocalDate.class }); }
    protected LocalDateType(SqlType sqlType) { super(sqlType); }
    protected LocalDateType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        try {
            return LocalDate.parse(defaultStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (NumberFormatException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType +
                    " parsing default LocalDate value: " + defaultStr, e);
        }
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getLocalDate(columnPos);
    }

    @Override
    public Object moveToNextValue(Object currentValue) {
        LocalDate date = (LocalDate) currentValue;
        return date.plusDays(1);
    }

    @Override
    public boolean isValidForField(Field field) {
        return (field.getType() == LocalDate.class);
    }
}
