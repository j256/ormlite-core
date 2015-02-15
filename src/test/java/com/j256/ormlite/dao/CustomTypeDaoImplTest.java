package com.j256.ormlite.dao;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import com.j256.ormlite.table.DatabaseTable;
import org.junit.Before;
import org.junit.Test;

import static com.j256.ormlite.dao.CustomTypeDaoImplTest.BarId.barId;
import static org.junit.Assert.*;

public class CustomTypeDaoImplTest extends BaseCoreTest {

    @Before
    public void setUp() throws Exception {
        DataPersisterManager.registerDataPersisters(BarIdType.getInstance());
    }


    @Test
    public void testCreate() throws Exception {
        Dao<Bar, BarId> dao = createDao(Bar.class, true);
        Bar bar = new Bar();
        BarId uniqueId = BarId.barId("unique_id");
        bar.id = uniqueId;
        String barValue = "Bar value";
        bar.value = barValue;

        assertEquals(1, dao.create(bar));
        Bar result = dao.queryForId(uniqueId);
        assertNotNull(result);
        assertEquals(barValue, result.value);
    }



    @Test
    public void testCreateOrUpdate() throws Exception {
        Dao<Bar, BarId> dao = createDao(Bar.class, true);
        Bar bar = new Bar();
        BarId uniqueId = BarId.barId("unique_id");
        bar.id = uniqueId;
        bar.value = "Bar value";
        CreateOrUpdateStatus status = dao.createOrUpdate(bar);
        assertTrue(status.isCreated());
        assertFalse(status.isUpdated());
        assertEquals(1, status.getNumLinesChanged());

        String changedBarValue = "changed bar value";
        bar.value = changedBarValue;
        status = dao.createOrUpdate(bar);
        assertFalse(status.isCreated());
        assertTrue(status.isUpdated());
        assertEquals(1, status.getNumLinesChanged());

        Bar found = dao.queryForId(uniqueId);
        assertEquals(changedBarValue, found.value);
    }


	/* ============================================================================================== */

    @DatabaseTable
    static class Bar {
        @DatabaseField(generatedId = false, id = true)
        BarId id;

        @DatabaseField
        String value;
    }

    static class BarId {
        private final String value;

        private BarId(String value) {
            this.value = value;
        }

        static BarId barId(String value) {
            return new BarId(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BarId barId = (BarId) o;

            if (!value.equals(barId.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            return value;
        }
    }

    static class BarIdType extends StringType {


        private static final BarIdType instance = new BarIdType();

        public static BarIdType getInstance() {
            return instance;
        }

        private BarIdType() {
            super(SqlType.STRING, new Class<?>[]{BarId.class});
        }

        @Override
        public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
            BarId dateTime = (BarId) javaObject;
            return dateTime.value;
        }

        @Override
        public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
            return barId(String.valueOf(sqlArg));
        }
    }
}
