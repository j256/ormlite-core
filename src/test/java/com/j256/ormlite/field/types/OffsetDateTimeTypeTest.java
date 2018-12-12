package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.DatabaseTable;

public class OffsetDateTimeTypeTest extends BaseTypeTest {

    private static final String DATE_TIME_COLUMN = "dateTime";

    @Test
    public void testDateTime() throws Exception {
        Class<DateTimeTable> clazz = DateTimeTable.class;
        Dao<DateTimeTable, Object> dao = createDao(clazz, true);
        OffsetDateTime val = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]x");
        String valStr = formatter.format(val);

        DateTimeTable foo = new DateTimeTable();
        foo.dateTime = val;
        assertEquals(1, dao.create(foo));

        testType(dao, foo, clazz, val, val, val, valStr, DataType.OFFSET_DATE_TIME, DATE_TIME_COLUMN, false,
                true, true, false, true, false,
                true, false);
    }

    @Test
    public void testDateNull() throws Exception {
        Class<DateTimeTable> clazz = DateTimeTable.class;
        Dao<DateTimeTable, Object> dao = createDao(clazz, true);
        DateTimeTable foo = new DateTimeTable();
        assertEquals(1, dao.create(foo));
        testType(dao, foo, clazz, null, null, null, null, DataType.OFFSET_DATE_TIME,
                DATE_TIME_COLUMN,
                false, true, true, false, true,
                false, true, false);
    }

    @Test(expected = DateTimeParseException.class)
    public void testDateParseInvalid() throws Exception {
        FieldType fieldType = FieldType.createFieldType(connectionSource, TABLE_NAME,
                DateTimeTable.class.getDeclaredField(DATE_TIME_COLUMN), DateTimeTable.class);
        DataType.OFFSET_DATE_TIME.getDataPersister().parseDefaultString(fieldType, "not valid datetime string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDateField() throws Exception {
        FieldType.createFieldType(connectionSource, TABLE_NAME, InvalidDate.class.getDeclaredField("notDateTime"),
                DateTimeTable.class);
    }

    /* ============================================================================================ */

    @DatabaseTable
    protected static class InvalidDate {
        @DatabaseField(dataType = DataType.OFFSET_DATE_TIME)
        String notDateTime;
    }

    @DatabaseTable(tableName = TABLE_NAME)
    protected static class DateTimeTable {
        @DatabaseField(columnName = DATE_TIME_COLUMN)
        OffsetDateTime dateTime;
    }
}
