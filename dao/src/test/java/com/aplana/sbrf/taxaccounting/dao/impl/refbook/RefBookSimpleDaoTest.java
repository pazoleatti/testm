package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Ref;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookSimpleDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookSimpleDaoTest {

    private static final int TABLE_TOTAL_RECORDS = 5;
    private static final String REF_BOOK_TABLE_NAME = "REF_BOOK_PERSON";
    private static final Long REF_BOOK_ID = 904L;

    @Autowired
    private RefBookSimpleDao dao;
    @Autowired
    private RefBookDao refBookDao;

    @Test
    public void getRecordsReturnsAllRecords() throws Exception {
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(REF_BOOK_ID, null, null, null, null, true);

        assertEquals(TABLE_TOTAL_RECORDS, data.getTotalCount());
        assertEquals(TABLE_TOTAL_RECORDS, data.size());
    }

    @Test
    public void getRecordsReturnsVersion1() throws Exception {
        Date version = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime();
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(REF_BOOK_ID, version, null, null, null, true);

        assertEquals(2, data.size());
    }

    @Test
    public void getRecordsReturnsVersion2() throws Exception {
        Date version = new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime();
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(REF_BOOK_ID, version, null, null, null, true);

        assertEquals(4, data.size());
    }

    @Test
    public void getRecordsReturnsLastVersion() throws Exception {
        Date version = new GregorianCalendar(2011, Calendar.JULY, 1).getTime();

        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(REF_BOOK_ID, version, null, null, null, true);

        assertEquals(1, data.size());
        Date expectedBirthday = new GregorianCalendar(1948, 1, 8).getTime();
        assertEquals(expectedBirthday, data.get(0).get("BIRTH_DATE").getDateValue());
    }

    @Test
    public void getRecordsReturnsPaginated0Count3() throws Exception {
        PagingParams pagingParams = new PagingParams(0, 3);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(REF_BOOK_ID, null, pagingParams, null, null, true);

        assertEquals(3, data.size());
    }

    @Test
    public void getRecordsReturnsPaginated2Count2() throws Exception {
        PagingParams pagingParams = new PagingParams(2, 2);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(REF_BOOK_ID, null, pagingParams, null, null, true);

        assertEquals(2, data.size());
    }

    @Test
    public void getRecordsReturnsEmptyOnWrongPageParams() throws Exception {
        PagingParams pagingParams = new PagingParams(99, 9);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(REF_BOOK_ID, null, pagingParams, null, null, true);

        assertEquals(0, data.size());
    }

    @Test
    public void getRecordsReturnsFiltered() throws Exception {
        String filter = "MIDDLE_NAME = 'Васильевич'";
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(REF_BOOK_ID, null, null, filter, null, true);

        assertEquals(2, data.size());
        assertEquals("Васильевич", data.get(0).get("MIDDLE_NAME").getStringValue());
        assertEquals("Васильевич", data.get(1).get("MIDDLE_NAME").getStringValue());
    }

    @Test
    public void getRecordsReturnsSorted() throws Exception {
        RefBookAttribute sortAttribute = new RefBookAttribute();
        sortAttribute.setAlias("FIRST_NAME");
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(REF_BOOK_ID, null, null, null, sortAttribute, true);
        PagingResult<Map<String, RefBookValue>> dataDescending = dao.getRecords(REF_BOOK_ID, null, null, null, sortAttribute, false);

        assertEquals("Петр", data.get(0).get("FIRST_NAME").getStringValue());
        assertEquals("Феофан", dataDescending.get(0).get("FIRST_NAME").getStringValue());
    }

    //    @Test
    public void getChildrenRecordsReturnsAll() throws Exception {
        PagingResult<Map<String, RefBookValue>> data = dao.getChildrenRecords(REF_BOOK_TABLE_NAME, REF_BOOK_ID, null,
                null, null, null, null);
        assertEquals(17, data.size());
        assertEquals(17, data.getTotalCount());
    }

    @Test
    public void getChildrenRecordsThrowsAnExceptionOnNotHierarchicalRefBook() throws Exception {
        try {
            dao.getChildrenRecords(REF_BOOK_TABLE_NAME, REF_BOOK_ID, null, null, null, null, null);
        } catch (IllegalArgumentException ex) {
            assertEquals("Справочник \"Физические лица\" (id=904) не является иерархичным", ex.getMessage());
        }

    }

    //    @Test
    public void getChildrenRecordsReturnsSome() throws Exception {
        PagingResult<Map<String, RefBookValue>> data = dao.getChildrenRecords(REF_BOOK_TABLE_NAME, REF_BOOK_ID, null,
                122934L, null, null, null);
        assertEquals(4, data.size());
        assertEquals(4, data.getTotalCount());
    }

    @Test
    public void getRowNumReturnsNum() throws Exception {
        Date version = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime();
        Long rowNum = dao.getRowNum(REF_BOOK_ID, version, 5L, null, null, true);

        assertEquals((Long) 2L, rowNum);
    }

    @Test
    public void getUniqueRecordIdsReturnsIds() throws Exception {
        List<Long> ids = dao.getUniqueRecordIds(REF_BOOK_ID, REF_BOOK_TABLE_NAME, null, null);

        assertEquals(TABLE_TOTAL_RECORDS, ids.size());
    }

    @Test
    public void getUniqueRecordIdsReturnsIdsWithVersionSet() throws Exception {
        Date version = new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime();
        List<Long> ids = dao.getUniqueRecordIds(REF_BOOK_ID, REF_BOOK_TABLE_NAME, version, null);

        assertEquals(2, ids.size());
    }

    @Test
    public void getRecordsCountReturnsAllRecordsCount() throws Exception {
        int result = dao.getRecordsCount(REF_BOOK_ID, REF_BOOK_TABLE_NAME, null, null);

        assertEquals(TABLE_TOTAL_RECORDS, result);
    }

    @Test
    public void getRecordsCountReturnsVersionedRecordsCount() throws Exception {
        Date version = new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime();
        int result = dao.getRecordsCount(REF_BOOK_ID, REF_BOOK_TABLE_NAME, version, null);

        assertEquals(4, result);
    }

    @Test
    public void getRecordVersionInfoReturnsVersionInfo() throws Exception {
        Date expectedVersionStart = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
        Date expectedVersionEnd = new GregorianCalendar(2010, Calendar.DECEMBER, 31).getTime();

        RefBookRecordVersion versionInfo = dao.getRecordVersionInfo(REF_BOOK_TABLE_NAME, 4L);

        assertEquals((Long) 4L, versionInfo.getRecordId());
        assertEquals(expectedVersionStart, versionInfo.getVersionStart());
        assertEquals(expectedVersionEnd, versionInfo.getVersionEnd());
        assertEquals(false, versionInfo.isVersionEndFake());
    }

    @Test
    public void getVersionsReturnsVersions() throws Exception {
        Date versionStart = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
        Date versionEnd = new GregorianCalendar(2013, Calendar.DECEMBER, 31).getTime();

        Date expectedDate1 = versionStart;
        Date expectedDate2 = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime();

        List<Date> versions = dao.getVersions(REF_BOOK_TABLE_NAME, versionStart, versionEnd);

        assertTrue(versions.contains(expectedDate1));
        assertTrue(versions.contains(expectedDate2));
    }

    @Test
    public void getRecordVersionsCountReturnsCount() throws Exception {
        int count = dao.getRecordVersionsCount(REF_BOOK_TABLE_NAME, 4L);

        assertEquals(2, count);
    }

    @Test
    public void getRecordIdReturnsId() throws Exception {
        Long expect3 = dao.getRecordId(REF_BOOK_TABLE_NAME, 3L);
        Long expect4 = dao.getRecordId(REF_BOOK_TABLE_NAME, 5L);

        assertEquals((Long) 3L, expect3);
        assertEquals((Long) 4L, expect4);
    }

    @Test(expected = DaoException.class)
    public void getRecordThrowsIfNotFound() throws Exception {
        dao.getRecordId(REF_BOOK_TABLE_NAME, 99L);
    }

    @Test
    public void getRecordVersionsByRecordIdReturnsVersions() throws Exception {
        Date d1948 = new GregorianCalendar(1948, Calendar.FEBRUARY, 8).getTime();
        Date d1946 = new GregorianCalendar(1946, Calendar.FEBRUARY, 8).getTime();
        List<Date> dates = new ArrayList<Date>();
        dates.add(d1946);
        dates.add(d1948);

        PagingResult<Map<String, RefBookValue>> data = dao.getRecordVersionsByRecordId(REF_BOOK_ID, 4L, null, null, null);

        assertEquals(2, data.size());
        for (Map<String, RefBookValue> refBook : data) {
            assertTrue(dates.contains(refBook.get("BIRTH_DATE").getDateValue()));
        }
    }

    @Test
    public void getRecordVersionsByRecordIdReturnsFiltered() throws Exception {
        Date d1948 = new GregorianCalendar(1948, Calendar.FEBRUARY, 8).getTime();

        PagingResult<Map<String, RefBookValue>> data = dao.getRecordVersionsByRecordId(REF_BOOK_ID, 4L, null, "BIRTH_PLACE = 'Калуга'", null);

        assertEquals(1, data.size());
        assertEquals(d1948, data.get(0).get("BIRTH_DATE").getDateValue());

    }

    public void getMatchedRecordsByUniqueAttributes() throws Exception {
        RefBook refBook = createRefBook();
        dao.getMatchedRecordsByUniqueAttributes(refBook, 4L, refBook.getAttributes(), createRecords());
    }

    private RefBook createRefBook() {
        RefBook refBook = new RefBook();
        refBook.setTableName(REF_BOOK_TABLE_NAME);
        refBook.setId(REF_BOOK_ID);
        refBook.setVersioned(true);
        refBook.setReadOnly(false);

        refBook.setAttributes(createAttributes());

        return refBook;
    }

    private List<RefBookAttribute> createAttributes() {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        return refBook.getAttributes();
    }

    private List<RefBookRecord> createRecords(){
        PagingResult<Map<String, RefBookValue>> existingRecordsValues = dao.getRecords(REF_BOOK_ID, null, null, null, null, true);

        RefBookRecord record = new RefBookRecord();
        record.setVersionTo(new Date());
        record.setValues(existingRecordsValues.get(0));

        RefBookRecord record2 = new RefBookRecord();
        record2.setVersionTo(new Date());
        record2.setValues(existingRecordsValues.get(1));

        return new ArrayList<RefBookRecord>(Arrays.asList(record, record2));
    }
}