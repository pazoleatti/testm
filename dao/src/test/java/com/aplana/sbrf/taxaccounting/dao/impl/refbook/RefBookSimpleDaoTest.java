package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookSimpleDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookSimpleDaoTest {

    private static final int TABLE_TOTAL_RECORDS = 5;
    private static final String REF_BOOK_TABLE_NAME = "REF_BOOK_PERSON";
    private static final Long REF_BOOK_ID = 904L;
    public static final String FAMILY_TABLE = "REF_BOOK_FAMILY";
    public static final long FAMILY_ID = 1983L;

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

    @Test
    public void getChildrenRecordsReturnsAll() throws Exception {
        PagingResult<Map<String, RefBookValue>> data = dao.getChildrenRecords(FAMILY_TABLE, FAMILY_ID, null,
                null, null, null, null);
        assertEquals(1, data.size());
        assertEquals(1, data.getTotalCount());
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
        PagingResult<Map<String, RefBookValue>> data = dao.getChildrenRecords(FAMILY_TABLE, FAMILY_ID, null,
                1L, null, null, null);
        assertEquals(2, data.size());
        assertEquals(2, data.getTotalCount());
    }

    @Test
    public void getRowNumReturnsNum() throws Exception {
        Date version = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime();
        Long rowNum = dao.getRowNum(REF_BOOK_ID, version, 3L, null, null, true);

        assertEquals((Long) 1L, rowNum);
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

    @Test
    public void getMatchedRecordsByUniqueAttributesFindsRecord1() throws Exception {
        RefBook refBook = createRefBook();
        List<Pair<Long, String>> matchedRecords = dao.getMatchedRecordsByUniqueAttributes(refBook, null, createRecords().get(0));

        assertEquals(1, matchedRecords.size());
        assertEquals((Long)1L, matchedRecords.get(0).getFirst());

        System.out.println(matchedRecords);
    }

    @Test
    public void getMatchedRecordsByUniqueAttributesFindsRecord2() throws Exception {
        RefBook refBook = createRefBook();
        List<Pair<Long, String>> matchedRecords = dao.getMatchedRecordsByUniqueAttributes(refBook, 4L, createRecords().get(1));

        assertEquals(1, matchedRecords.size());
        assertEquals((Long)2L, matchedRecords.get(0).getFirst());
    }

    @Test
    public void getMatchedRecordsByUniqueAttributesNotFindsRecord() throws Exception {
        RefBook refBook = createRefBook();
        List<Pair<Long, String>> matchedRecords = dao.getMatchedRecordsByUniqueAttributes(refBook, 2L, createRecords().get(1));

        assertEquals(0, matchedRecords.size());
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

    @Test
    public void checkConflictValuesVersionsReturnsConflict() throws Exception {
        List<Long> conflicts = dao.checkConflictValuesVersions(createRefBook(), createValues(), new Date(0), new Date());
        assertEquals(1, conflicts.size());
        assertEquals((Long)1L, conflicts.get(0));
    }

    private List<Pair<Long, String>> createValues(){
        Pair<Long, String> p1 = new Pair<Long, String>(1L, "ИНН в Российской Федерации,ИНН в стране гражданства,СНИЛС");
        return new ArrayList<Pair<Long, String>>(Arrays.asList(p1));
    }

    @Test
    public void checkConflictValuesVersionsReturnsNoConflict() throws Exception {
        Date d2013 = new GregorianCalendar(2013,0,1).getTime();
        Date d2015 = new GregorianCalendar(2015,0,1).getTime();
        List<Long> conflicts = dao.checkConflictValuesVersions(createRefBook(), createValues(), d2013, d2015);
        assertEquals(0, conflicts.size());
    }

    @Test
    public void checkParentConflictReturnsConflictIfVersionIsEarlier() throws Exception {
        RefBook refBook = createHierarchyRefBook();
        RefBookRecord record = createHierarchyRecord("Афанасий", "Иванович", "Иванов", 1L, new GregorianCalendar(2015,0,1).getTime(), null);

        Date versionFrom = new GregorianCalendar(2013, Calendar.MARCH, 1).getTime();
        List<Pair<Long, Integer>> conflict = dao.checkParentConflict(refBook, versionFrom, new ArrayList<RefBookRecord>(Arrays.asList(record)));
        Pair<Long, Integer> expected = new Pair<Long, Integer>(1L, -1);
        System.out.println(conflict);

        assertEquals(1, conflict.size());
        assertEquals(expected, conflict.get(0));
    }

    @Test
    public void checkParentConflictNotReturnsConflict() throws Exception {
        RefBook refBook = createHierarchyRefBook();
        RefBookRecord record = createHierarchyRecord("Афанасий", "Иванович", "Иванов", 1L, new GregorianCalendar(2019,0,1).getTime(), null);

        Date versionFrom = new GregorianCalendar(2017, Calendar.MARCH, 1).getTime();
        List<Pair<Long, Integer>> conflict = dao.checkParentConflict(refBook, versionFrom, new ArrayList<RefBookRecord>(Arrays.asList(record)));
        Pair<Long, Integer> expected = new Pair<Long, Integer>(1L, 0);

        assertEquals(1, conflict.size());
        assertEquals(expected, conflict.get(0));
    }

    private RefBookRecord createHierarchyRecord(String firstName, String lastName, String middleName, Long parentId, Date version, Long recordId)  {
        RefBookValue fNameVal = new RefBookValue(RefBookAttributeType.STRING, firstName);
        RefBookValue lNameVal = new RefBookValue(RefBookAttributeType.STRING, lastName);
        RefBookValue mNameVal = new RefBookValue(RefBookAttributeType.STRING, middleName);
        RefBookValue parentVal = new RefBookValue(RefBookAttributeType.REFERENCE, parentId);
        RefBookValue birthVal = new RefBookValue(RefBookAttributeType.DATE, new GregorianCalendar(1970,3,5).getTime());
        RefBookValue citizVal = new RefBookValue(RefBookAttributeType.REFERENCE, 266174099L);
        RefBookValue socVal = new RefBookValue(RefBookAttributeType.STRING, "1");
        RefBookValue pensVal = new RefBookValue(RefBookAttributeType.STRING, "1");
        RefBookValue medVal = new RefBookValue(RefBookAttributeType.STRING, "1");
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        values.put("FIRST_NAME", fNameVal);
        values.put("LAST_NAME", lNameVal);
        values.put("MIDDLE_NAME", mNameVal);
        values.put(RefBook.RECORD_PARENT_ID_ALIAS, parentVal);
        values.put("BIRTH_DATE", birthVal);
        values.put("CITIZENSHIP", citizVal);
        values.put("SOCIAL", socVal);
        values.put("PENSION", pensVal);
        values.put("MEDICAL", medVal);

        RefBookRecord record = new RefBookRecord();
        record.setVersionTo(version);
        record.setValues(values);
        if (recordId != null) {
            record.setRecordId(recordId);
        }
        return record;
    }

    private RefBook createHierarchyRefBook() {
        RefBook refBook = new RefBook();
        refBook.setTableName("REF_BOOK_FAMILY");
        refBook.setId(1983L);
        refBook.setType(1);
        refBook.setReadOnly(false);
        refBook.setVersioned(true);
        return refBook;
    }

    @Test
    public void checkCrossVersionsExecutes() throws Exception {
        List<CheckCrossVersionsResult> resultList = dao.checkCrossVersions(createRefBook(), 4L, new Date(0), new Date(), null);
        assertNotNull(resultList);
    }

    @Test
    public void isVersionUsedLikeParentExecutes() throws Exception {
        List<Pair<Date, Date>> list = dao.isVersionUsedLikeParent(createHierarchyRefBook(), 1L, new Date(0));
        assertNotNull(list);
    }

    @Test
    public void getNextVersionExecutes() throws Exception {
        dao.getNextVersion(createRefBook(), new Date(0), "SEX = 1");
    }

    @Test
    public void getNextVersionReturnsNextVersion() throws Exception {
        RefBookRecordVersion nextVersion = dao.getNextVersion(createRefBook(), 1L, new Date(0));
        Date expectedStartDate = new GregorianCalendar(2017, 0, 9).getTime();

        assertEquals(expectedStartDate, nextVersion.getVersionStart());
        assertEquals(null, nextVersion.getVersionEnd());
        assertEquals((Long)1L, nextVersion.getRecordId());
    }

    @Test
    public void getNextVersionReturnsNextVersion2() throws Exception {
        RefBookRecordVersion nextVersion = dao.getNextVersion(createRefBook(), 4L, new Date(0));
        Date expectedStartDate = new GregorianCalendar(2010, 0, 1).getTime();
        Date expectedEndDate = new GregorianCalendar(2010, 11, 31).getTime();

        assertEquals(expectedStartDate, nextVersion.getVersionStart());
        assertEquals(expectedEndDate, nextVersion.getVersionEnd());
        assertEquals((Long)4L, nextVersion.getRecordId());
    }

    @Test
    public void getNextVersionReturnsNextVersion3() throws Exception {
        Date versionFrom = new GregorianCalendar(2010,6,22).getTime();
        RefBookRecordVersion nextVersion = dao.getNextVersion(createRefBook(), 4L, versionFrom);
        Date expectedStartDate = new GregorianCalendar(2011, 0, 1).getTime();
        Date expectedEndDate = null;

        assertEquals(expectedStartDate, nextVersion.getVersionStart());
        assertEquals(expectedEndDate, nextVersion.getVersionEnd());
        assertEquals((Long)5L, nextVersion.getRecordId());
    }

    @Test
    public void createFakeRecordVersionInsertsData() throws Exception {
        Date fakeVersion = new GregorianCalendar(2020,5,5).getTime();
        Date versionFrom = new GregorianCalendar(2010,5,5).getTime();
        Date expectedVersionEnd = new GregorianCalendar(2020,5,4).getTime();
        RefBook refBook = createRefBook();

        dao.createFakeRecordVersion(refBook, 4L, fakeVersion);

        PagingResult<Map<String, RefBookValue>> records = dao.getRecordVersionsByRecordId(refBook.getId(), 4L, null, null, null);
        RefBookRecordVersion nextVersion = dao.getNextVersion(refBook, 4L, versionFrom);
        assertEquals(2, records.getTotalCount());
        assertEquals(expectedVersionEnd, nextVersion.getVersionEnd());
    }

    @Test
    public void createRecordVersionInsertsRecords() throws Exception {
        Date version = new GregorianCalendar(2020,5,14).getTime();
        RefBook refBook = createRefBook();

        dao.createRecordVersion(refBook, version, VersionedObjectStatus.NORMAL, createRecordsForCreateRecordsVersion());
        PagingResult<Map<String, RefBookValue>> result1 = dao.getRecordVersionsByRecordId(refBook.getId(), 1L, null, null, null);
        PagingResult<Map<String, RefBookValue>> result2 = dao.getRecordVersionsByRecordId(refBook.getId(), 4L, null, null, null);

        assertEquals(2, result1.size());
        assertEquals(3, result2.size());
    }

    private List<RefBookRecord> createRecordsForCreateRecordsVersion() {
        List<RefBookRecord> records = new ArrayList<RefBookRecord>();
        records.add(createHierarchyRecord("Федор", "Иванов", "Семёнович", null, null, 1L));
        records.add(createHierarchyRecord("Чарльз", "Иванов", "Васильевич", null, null, 4L));

        return records;
    }
}