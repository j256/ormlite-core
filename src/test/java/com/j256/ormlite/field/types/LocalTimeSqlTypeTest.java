package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.DatabaseTable;

public class LocalTimeSqlTypeTest extends BaseTypeTest {

    private static final String TIME_COLUMN = "time";

    @Test
    public void testTime() throws Exception {
        Class<TimeTable> clazz = TimeTable.class;
        Dao<TimeTable, Object> dao = createDao(clazz, true);
        LocalTime val = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
        Time val2 = Time.valueOf(val);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String valStr = formatter.format(val);
        TimeTable foo = new TimeTable();
        foo.time = val;
        assertEquals(1, dao.create(foo));

        testType(dao, foo, clazz, val, val2, val2, valStr, DataType.LOCAL_TIME_SQL, TIME_COLUMN, false,
                true, true, false, true, false,
                true, false);
    }

    @Test
    public void testDateNull() throws Exception {
        Class<TimeTable> clazz = TimeTable.class;
        Dao<TimeTable, Object> dao = createDao(clazz, true);
        TimeTable foo = new TimeTable();
        assertEquals(1, dao.create(foo));
        testType(dao, foo, clazz, null, null, null, null, DataType.LOCAL_TIME_SQL, TIME_COLUMN,
                false, true, true, false, true,
                false, true, false);
    }

    @Test(expected = DateTimeParseException.class)
    public void testDateParseInvalid() throws Exception {
        FieldType fieldType = FieldType.createFieldType(connectionSource, TABLE_NAME,
                TimeTable.class.getDeclaredField(TIME_COLUMN), TimeTable.class);
        DataType.LOCAL_TIME_SQL.getDataPersister().parseDefaultString(fieldType, "not valid time string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDateField() throws Exception {
        FieldType.createFieldType(connectionSource, TABLE_NAME, InvalidDate.class.getDeclaredField("notTime"),
                TimeTable.class);
    }

    /* ============================================================================================ */

    @DatabaseTable
    protected static class InvalidDate {
        @DatabaseField(dataType = DataType.LOCAL_TIME_SQL)
        String notTime;
    }

    @DatabaseTable(tableName = TABLE_NAME)
    protected static class TimeTable {
        @DatabaseField(columnName = TIME_COLUMN, dataType = DataType.LOCAL_TIME_SQL)
        LocalTime time;
    }
}
