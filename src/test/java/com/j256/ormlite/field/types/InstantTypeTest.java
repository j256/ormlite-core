package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.DatabaseTable;

public class InstantTypeTest extends BaseTypeTest {

    private static final String INSTANT_COLUMN = "instant";

    @Test
    public void testInstant() throws Exception {
        Class<InstantTable> clazz = InstantTable.class;
        Dao<InstantTable, Object> dao = createDao(clazz, true);
        Instant val = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]x");
        OffsetDateTime val2 = OffsetDateTime.ofInstant(val, ZoneOffset.UTC);
        String valStr = formatter.format(val2);
        InstantTable foo = new InstantTable();
        foo.instant = val;
        assertEquals(1, dao.create(foo));

        testType(dao, foo, clazz, val, val2, val2, valStr, DataType.INSTANT, INSTANT_COLUMN, false,
                true, true, false, true, false,
                true, false);
    }

    @Test
    public void testDateNull() throws Exception {
        Class<InstantTable> clazz = InstantTable.class;
        Dao<InstantTable, Object> dao = createDao(clazz, true);
        InstantTable foo = new InstantTable();
        assertEquals(1, dao.create(foo));
        testType(dao, foo, clazz, null, null, null, null, DataType.INSTANT,
                INSTANT_COLUMN,
                false, true, true, false, true,
                false, true, false);
    }

    @Test(expected = DateTimeParseException.class)
    public void testDateParseInvalid() throws Exception {
        FieldType fieldType = FieldType.createFieldType(connectionSource, TABLE_NAME,
                InstantTable.class.getDeclaredField(INSTANT_COLUMN), InstantTable.class);
        DataType.INSTANT.getDataPersister().parseDefaultString(fieldType, "not valid instant string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDateField() throws Exception {
        FieldType.createFieldType(connectionSource, TABLE_NAME, InvalidDate.class.getDeclaredField("notInstant"),
                InstantTable.class);
    }

    /* ============================================================================================ */

    @DatabaseTable
    protected static class InvalidDate {
        @DatabaseField(dataType = DataType.INSTANT)
        String notInstant;
    }

    @DatabaseTable(tableName = TABLE_NAME)
    protected static class InstantTable {
        @DatabaseField(columnName = INSTANT_COLUMN)
        Instant instant;
    }
}
