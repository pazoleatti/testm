package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Тесты с версионированием неработоспособны в hsqldb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookSimpleDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookSimpleDaoTest {

    private static final int TABLE_TOTAL_RECORDS = 5;
    private static final String TABLE_NAME = "REF_BOOK_PERSON";
    private static final Long REF_BOOK_ID = 904L;

    @Autowired
    private RefBookSimpleDao dao;

    @Autowired
    private RefBookDao refBookDao;

    @Test
    public void getRecordsReturnsAllRecords() throws Exception {
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(TABLE_NAME, REF_BOOK_ID, null, null, null, null, true);

        assertEquals(TABLE_TOTAL_RECORDS, data.getTotalCount());
        assertEquals(TABLE_TOTAL_RECORDS, data.size());
    }

    @Test
    public void getRecordsReturnsVersion() throws Exception {
        Date version = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime();
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(TABLE_NAME, REF_BOOK_ID, version, null, null, null, true);

        assertEquals(2, data.size());
    }

    @Test
    public void getRecordsReturnsPaginated0Count3() throws Exception {
        PagingParams pagingParams = new PagingParams(0, 3);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(TABLE_NAME, REF_BOOK_ID, null, pagingParams, null, null, true);

        assertEquals(3, data.size());
    }

    @Test
    public void getRecordsReturnsPaginated2Count2() throws Exception {
        PagingParams pagingParams = new PagingParams(2, 2);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(TABLE_NAME, REF_BOOK_ID, null, pagingParams, null, null, true);

        assertEquals(2, data.size());
    }

    @Test
    public void getRecordsReturnsEmptyOnWrongPageParams() throws Exception {
        PagingParams pagingParams = new PagingParams(99, 9);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(TABLE_NAME, REF_BOOK_ID, null, pagingParams, null, null, true);

        assertEquals(0, data.size());
    }

    @Test
    public void getRecordsReturnsFiltered() throws Exception {
        String filter = "MIDDLE_NAME = 'Васильевич'";
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(TABLE_NAME, REF_BOOK_ID, null, null, filter, null, true);

        assertEquals(2, data.size());
        assertEquals("Васильевич", data.get(0).get("MIDDLE_NAME").getStringValue());
        assertEquals("Васильевич", data.get(1).get("MIDDLE_NAME").getStringValue());
    }

    /*
    Сортировка не работает с hsqdlb
     */
//    @Test
    public void getRecordsReturnsSorted() throws Exception {
        RefBookAttribute sortAttribute = new RefBookAttribute();
        sortAttribute.setAlias("FIRST_NAME");
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(TABLE_NAME, REF_BOOK_ID, null, null, null, sortAttribute, true);
        PagingResult<Map<String, RefBookValue>> dataDescending = dao.getRecords(TABLE_NAME, REF_BOOK_ID, null, null, null, sortAttribute, false);

        assertEquals("Георгий", data.get(0).get("FIRST_NAME").getStringValue());
        assertEquals("Феофан", dataDescending.get(0).get("FIRST_NAME").getStringValue());
    }

    @Test
    public void getChildrenRecordsReturnsAll() throws Exception {
        PagingResult<Map<String, RefBookValue>> data = dao.getChildrenRecords(TABLE_NAME, REF_BOOK_ID, null,
                null, null, null, null);
        assertEquals(17, data.size());
        assertEquals(17, data.getTotalCount());
    }

    //    @Test
    public void getChildrenRecordsReturnsSome() throws Exception {
        PagingResult<Map<String, RefBookValue>> data = dao.getChildrenRecords(TABLE_NAME, REF_BOOK_ID, null,
                122934L, null, null, null);
        assertEquals(4, data.size());
        assertEquals(4, data.getTotalCount());
    }

    @Test
    public void getRowNumReturnsNum() throws Exception {
        Date version = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime();
        Long rowNum = dao.getRowNum(REF_BOOK_ID, version, 5L, null, null, true);

        assertEquals((Long) 4L, rowNum);
    }

    @Test
    public void getUniqueRecordIdsReturnsIds() throws Exception {
        List<Long> ids = dao.getUniqueRecordIds(REF_BOOK_ID, TABLE_NAME, null, null);
        System.out.println(ids);
        assertEquals(TABLE_TOTAL_RECORDS, ids.size());
    }

    @Test
    public void getUniqueRecordIdsReturnsIdsWithVersionSet() throws Exception {
        Date version = new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime();
        List<Long> ids = dao.getUniqueRecordIds(REF_BOOK_ID, TABLE_NAME, version, null);

        assertEquals(TABLE_TOTAL_RECORDS - 1, ids.size());
    }

    @Test
    public void getRecordsCountReturnsAllRecordsCount() throws Exception {
        int result = dao.getRecordsCount(REF_BOOK_ID, TABLE_NAME, null, null);

        assertEquals(TABLE_TOTAL_RECORDS, result);
    }

    @Test
    public void getRecordsCountReturnsVersionedRecordsCount() throws Exception {
        Date version = new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime();
        int result = dao.getRecordsCount(REF_BOOK_ID, TABLE_NAME, version, null);

        assertEquals(4, result);
    }

    @Test
    public void getRecordVersionInfoReturnsVersionInfo() throws Exception {
        Date expectedVersionStart = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
        Date expectedVersionEnd = new GregorianCalendar(2010, Calendar.DECEMBER, 31).getTime();

        RefBookRecordVersion versionInfo = dao.getRecordVersionInfo(TABLE_NAME, 4L);

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

        List<Date> versions = dao.getVersions(TABLE_NAME, versionStart, versionEnd);

        assertTrue(versions.contains(expectedDate1));
        assertTrue(versions.contains(expectedDate2));
    }

    @Test
    public void getRecordVersionsCountReturnsCount() throws Exception {
        int count = dao.getRecordVersionsCount(TABLE_NAME, 4L);

        assertEquals(2, count);
    }
}