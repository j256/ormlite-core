package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.DatabaseTable;

public class LocalDateTypeTest extends BaseTypeTest {

    private static final String DATE_COLUMN = "date";

    @Test
    public void testDate() throws Exception {
        Class<DateTable> clazz = DateTable.class;
        Dao<DateTable, Object> dao = createDao(clazz, true);
        LocalDate val = LocalDate.now();
        LocalDate val2 = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String valStr = formatter.format(val);
        DateTable foo = new DateTable();
        foo.date = val;
        assertEquals(1, dao.create(foo));

        testType(dao, foo, clazz, val, val2, val2, valStr, DataType.LOCAL_DATE, DATE_COLUMN, false,
                true, true, false, true, false,
                true, false);
    }

    @Test
    public void testDateNull() throws Exception {
        Class<DateTable> clazz = DateTable.class;
        Dao<DateTable, Object> dao = createDao(clazz, true);
        DateTable foo = new DateTable();
        assertEquals(1, dao.create(foo));
        testType(dao, foo, clazz, null, null, null, null, DataType.LOCAL_DATE, DATE_COLUMN,
                false, true, true, false, true,
                false, true, false);
    }

    @Test(expected = DateTimeParseException.class)
    public void testDateParseInvalid() throws Exception {
        FieldType fieldType = FieldType.createFieldType(connectionSource, TABLE_NAME,
                DateTable.class.getDeclaredField(DATE_COLUMN), DateTable.class);
        DataType.LOCAL_DATE.getDataPersister().parseDefaultString(fieldType, "not valid date string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDateField() throws Exception {
        FieldType.createFieldType(connectionSource, TABLE_NAME, InvalidDate.class.getDeclaredField("notDate"),
                DateTable.class);
    }

    /* ============================================================================================ */

    @DatabaseTable
    protected static class InvalidDate {
        @DatabaseField(dataType = DataType.LOCAL_DATE)
        String notDate;
    }

    @DatabaseTable(tableName = TABLE_NAME)
    protected static class DateTable {
        @DatabaseField(columnName = DATE_COLUMN)
        LocalDate date;
    }
}
