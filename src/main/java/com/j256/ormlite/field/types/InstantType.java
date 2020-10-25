package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;

/**
 * A custom persister that is able to store the java.time.Instant class in the database as Timestamp With Timezone object.
 * This class does not have a SQL backup counter-part, the database should support JDBC 4.2 for it to be used.
 * Instant is also not a part of JDBC specification, persister converts Instant to OffsetDateTime with timezone fixed at UTC
 *
 * @author graynk
 */
public class InstantType extends OffsetDateTimeType {

    private static final InstantType singleton = isJavaTimeSupported() ? new InstantType() : null;
    public static InstantType getSingleton() { return singleton; }
    private InstantType() { super(SqlType.OFFSET_DATE_TIME, new Class<?>[] { Instant.class }); }
    protected InstantType(SqlType sqlType, Class<?>[] classes) { super(sqlType, classes); }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        OffsetDateTime value = (OffsetDateTime) sqlArg;
        return value.toInstant();
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        Instant instant = (Instant) javaObject;
        // ZoneOffset.UTC is evaluated at InstantType creation, fails on Java 6. Using ZoneId.of() instead
        return OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

    @Override
    public Object moveToNextValue(Object currentValue) {
        Instant datetime = (Instant) currentValue;
        return datetime.plusNanos(1);
    }

    @Override
    public boolean isValidForField(Field field) {
        return (field.getType() == Instant.class);
    }
}
