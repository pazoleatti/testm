package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckCrossVersionsResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


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
    private RefBookSimpleDaoImpl dao;
    @Autowired
    private RefBookDao refBookDao;

    @Test
    public void getRecordsReturnsAllRecords() throws Exception {
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, null, null, null, true);

        assertEquals(TABLE_TOTAL_RECORDS, data.getTotalCount());
        assertEquals(TABLE_TOTAL_RECORDS, data.size());
    }

    @Test
    public void getRecordsReturnsVersion1() throws Exception {
        Date version = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime();
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), version, null, null, null, true);

        assertEquals(2, data.size());
    }

    @Test
    public void getRecordsReturnsVersion2() throws Exception {
        Date version = new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime();
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), version, null, null, null, true);

        assertEquals(4, data.size());
    }

    @Test
    public void getRecordsReturnsLastVersion() throws Exception {
        Date version = new GregorianCalendar(2011, Calendar.JULY, 1).getTime();

        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), version, null, null, null, true);

        assertEquals(1, data.size());
        Date expectedBirthday = new GregorianCalendar(1948, 1, 8).getTime();
        assertEquals(expectedBirthday, data.get(0).get("BIRTH_DATE").getDateValue());
    }

    @Test
    public void getRecordsReturnsPaginated0Count3() throws Exception {
        PagingParams pagingParams = new PagingParams(0, 3);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, pagingParams, null, null, true);

        assertEquals(3, data.size());
    }

    @Test
    public void getRecordsReturnsPaginated2Count2() throws Exception {
        PagingParams pagingParams = new PagingParams(2, 2);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, pagingParams, null, null, true);

        assertEquals(2, data.size());
    }

    @Test
    public void getRecordsReturnsEmptyOnWrongPageParams() throws Exception {
        PagingParams pagingParams = new PagingParams(99, 9);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, pagingParams, null, null, true);

        assertEquals(0, data.size());
    }

    @Test
    public void getRecordsReturnsFiltered() throws Exception {
        String filter = "MIDDLE_NAME = 'Васильевич'";
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, null, filter, null, true);

        assertEquals(2, data.size());
        assertEquals("Васильевич", data.get(0).get("MIDDLE_NAME").getStringValue());
        assertEquals("Васильевич", data.get(1).get("MIDDLE_NAME").getStringValue());
    }

    @Test
    public void getRecordsReturnsSorted() throws Exception {
        RefBookAttribute sortAttribute = new RefBookAttribute();
        sortAttribute.setAlias("FIRST_NAME");
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, null, null, sortAttribute, true);
        PagingResult<Map<String, RefBookValue>> dataDescending = dao.getRecords(createRefBook(), null, null, null, sortAttribute, false);

        assertEquals("Петр", data.get(0).get("FIRST_NAME").getStringValue());
        assertEquals("Феофан", dataDescending.get(0).get("FIRST_NAME").getStringValue());
    }

    @Test
    public void getChildrenRecordsReturnsAll() throws Exception {
        PagingResult<Map<String, RefBookValue>> data = dao.getChildrenRecords(createHierarchyRefBook(), null,
                null, null, null, null);
        assertEquals(1, data.size());
        assertEquals(1, data.getTotalCount());
    }

    @Test
    public void getChildrenRecordsThrowsAnExceptionOnNotHierarchicalRefBook() throws Exception {
        try {
            dao.getChildrenRecords(createRefBook(), null, null, null, null, null);
        } catch (IllegalArgumentException ex) {
            assertEquals("Справочник \"Физические лица\" (id=904) не является иерархичным", ex.getMessage());
        }
    }

//    @Test
    public void getChildrenRecordsReturnsSome() throws Exception {
        PagingResult<Map<String, RefBookValue>> data = dao.getChildrenRecords(createHierarchyRefBook(), null,
                1L, null, null, null);
        assertEquals(2, data.size());
        assertEquals(2, data.getTotalCount());
    }

    @Test
    public void getRowNumReturnsNum() throws Exception {
        Date version = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime();
        Long rowNum = dao.getRowNum(createRefBook(), version, 3L, null, null, true);

        assertEquals((Long) 1L, rowNum);
    }

    @Test
    public void getUniqueRecordIdsReturnsIds() throws Exception {
        List<Long> ids = dao.getUniqueRecordIds(createRefBook(), null, null);

        assertEquals(TABLE_TOTAL_RECORDS, ids.size());
    }

    @Test
    public void getUniqueRecordIdsReturnsIdsWithVersionSet() throws Exception {
        Date version = new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime();
        List<Long> ids = dao.getUniqueRecordIds(createRefBook(), version, null);

        assertEquals(2, ids.size());
    }

    @Test
    public void getRecordsCountReturnsAllRecordsCount() throws Exception {
        int result = dao.getRecordsCount(createRefBook(), null, null);

        assertEquals(TABLE_TOTAL_RECORDS, result);
    }

    @Test
    public void getRecordsCountReturnsVersionedRecordsCount() throws Exception {
        Date version = new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime();
        int result = dao.getRecordsCount(createRefBook(), version, null);

        assertEquals(4, result);
    }

    @Test
    public void getRecordVersionInfoReturnsVersionInfo() throws Exception {
        Date expectedVersionStart = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
        Date expectedVersionEnd = new GregorianCalendar(2010, Calendar.DECEMBER, 31).getTime();

        RefBookRecordVersion versionInfo = dao.getRecordVersionInfo(createRefBook(), 4L);

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

        List<Date> versions = dao.getVersions(createRefBook(), versionStart, versionEnd);

        assertTrue(versions.contains(expectedDate1));
        assertTrue(versions.contains(expectedDate2));
    }

    @Test
    public void getRecordVersionsCountReturnsCount() throws Exception {
        int count = dao.getRecordVersionsCount(createRefBook(), 4L);

        assertEquals(2, count);
    }

    @Test
    public void getRecordIdReturnsId() throws Exception {
        Long expect3 = dao.getRecordId(createRefBook(), 3L);
        Long expect4 = dao.getRecordId(createRefBook(), 5L);

        assertEquals((Long) 3L, expect3);
        assertEquals((Long) 4L, expect4);
    }

    @Test(expected = DaoException.class)
    public void getRecordThrowsIfNotFound() throws Exception {
        dao.getRecordId(createRefBook(), 99L);
    }

    @Test
    public void getRecordVersionsByRecordIdReturnsVersions() throws Exception {
        Date d1948 = new GregorianCalendar(1948, Calendar.FEBRUARY, 8).getTime();
        Date d1946 = new GregorianCalendar(1946, Calendar.FEBRUARY, 8).getTime();
        List<Date> dates = new ArrayList<Date>();
        dates.add(d1946);
        dates.add(d1948);

        PagingResult<Map<String, RefBookValue>> data = dao.getRecordVersionsByRecordId(createRefBook(), 4L, null, null, null);

        assertEquals(2, data.size());
        for (Map<String, RefBookValue> refBook : data) {
            assertTrue(dates.contains(refBook.get("BIRTH_DATE").getDateValue()));
        }
    }

    @Test
    public void getRecordVersionsByRecordIdReturnsFiltered() throws Exception {
        Date d1948 = new GregorianCalendar(1948, Calendar.FEBRUARY, 8).getTime();

        PagingResult<Map<String, RefBookValue>> data = dao.getRecordVersionsByRecordId(createRefBook(), 4L, null, "BIRTH_PLACE = 'Калуга'", null);

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
        refBook.setName("Физические лица");

        refBook.setAttributes(createAttributes());

        return refBook;
    }

    private List<RefBookAttribute> createAttributes() {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        return refBook.getAttributes();
    }

    private List<RefBookRecord> createRecords(){
        PagingResult<Map<String, RefBookValue>> existingRecordsValues = dao.getRecords(createRefBook(), null, null, null, null, true);

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
        RefBookValue socVal = new RefBookValue(RefBookAttributeType.NUMBER, 1);
        RefBookValue pensVal = new RefBookValue(RefBookAttributeType.NUMBER, 1);
        RefBookValue medVal = new RefBookValue(RefBookAttributeType.NUMBER, 1);
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
        refBook.setAttributes(refBookDao.getAttributes(1983L));
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
    public void getPreviousVersionReturnsPrevVersion() throws Exception {
        Date versionFrom = new GregorianCalendar(2011,0,1).getTime();
        Date expectedStartDate = new GregorianCalendar(2010, 0, 1).getTime();
        Date expectedEndDate = new GregorianCalendar(2010, 11, 31).getTime();

        RefBookRecordVersion previousVersion = dao.getPreviousVersion(createRefBook(), 4L, versionFrom);

        if (previousVersion == null) {
            fail("dao returns null");
        }

        assertEquals(expectedStartDate, previousVersion.getVersionStart());
        assertEquals(expectedEndDate, previousVersion.getVersionEnd());
    }

    @Test
    public void createFakeRecordVersionInsertsData() throws Exception {
        Date fakeVersion = new GregorianCalendar(2020,5,5).getTime();
        Date versionFrom = new GregorianCalendar(2010,5,5).getTime();
        Date expectedVersionEnd = new GregorianCalendar(2020,5,4).getTime();
        RefBook refBook = createRefBook();

        dao.createFakeRecordVersion(refBook, 4L, fakeVersion);

        PagingResult<Map<String, RefBookValue>> records = dao.getRecordVersionsByRecordId(refBook, 4L, null, null, null);
        RefBookRecordVersion nextVersion = dao.getNextVersion(refBook, 4L, versionFrom);
        assertEquals(2, records.getTotalCount());
        assertEquals(expectedVersionEnd, nextVersion.getVersionEnd());
    }

    @Test
    public void createRecordVersionInsertsRecords() throws Exception {
        Date version = new GregorianCalendar(2020,5,14).getTime();
        RefBook refBook = createRefBook();

        dao.createRecordVersion(refBook, version, VersionedObjectStatus.NORMAL, createRecordsForCreateRecordsVersion());
        PagingResult<Map<String, RefBookValue>> result1 = dao.getRecordVersionsByRecordId(refBook, 1L, null, null, null);
        PagingResult<Map<String, RefBookValue>> result2 = dao.getRecordVersionsByRecordId(refBook, 4L, null, null, null);

        assertEquals(2, result1.size());
        assertEquals(3, result2.size());
    }

    private List<RefBookRecord> createRecordsForCreateRecordsVersion() {
        List<RefBookRecord> records = new ArrayList<RefBookRecord>();
        records.add(createHierarchyRecord("Федор", "Иванов", "Семёнович", null, null, 1L));
        records.add(createHierarchyRecord("Чарльз", "Иванов", "Васильевич", null, null, 4L));

        return records;
    }

    @Test
    public void getRecordVersionsReturnsVersions() throws Exception {
        Date expectedDate1 = new GregorianCalendar(2010, 0, 1).getTime();
        Date expectedDate2 = new GregorianCalendar(2011, 0, 1).getTime();

        PagingResult<Map<String, RefBookValue>> recordVersions = dao.getRecordVersions(createRefBook(), 5L, null, null, null, true);

        assertEquals(2, recordVersions.getTotalCount());
        assertEquals(expectedDate1, recordVersions.get(0).get("record_version_from").getDateValue());
        assertEquals(expectedDate2, recordVersions.get(1).get("record_version_from").getDateValue());
    }

    @Test
    public void getRecordDataReturnsData() throws Exception {
        Date expectedBirthday = new GregorianCalendar(1948,1,8).getTime();

        Map<String, RefBookValue> recordData = dao.getRecordData(createRefBook(), 5L);

        assertEquals(expectedBirthday, recordData.get("BIRTH_DATE").getDateValue());
        assertEquals("Васильевич", recordData.get("MIDDLE_NAME").getStringValue());
        assertEquals("Калуга", recordData.get("BIRTH_PLACE").getStringValue());
    }

    @Test
    public void getRecordData2ReturnsData() throws Exception {
        Map<Long, Map<String, RefBookValue>> recordData = dao.getRecordData(createRefBook(), new ArrayList<Long>(Arrays.asList(1L, 2L)));

        assertEquals(2, recordData.size());
        assertEquals("Федор", recordData.get(1L).get("FIRST_NAME").getStringValue());
        assertEquals("Петр", recordData.get(2L).get("FIRST_NAME").getStringValue());
    }

    @Test
    public void findRecordReturnsId() throws Exception {
        Date version = new GregorianCalendar(2011, 0, 1).getTime();

        Long id = dao.findRecord(createRefBook(), 4L, version);

        assertEquals((Long)5L, id);
    }

    @Test
    public void getRelatedVersionsReturnsId() throws Exception {
        Date fakeVersion = new GregorianCalendar(2020,5,5).getTime();
        dao.createFakeRecordVersion(createRefBook(), 4L, fakeVersion);

        List<Long> relatedVersions = dao.getRelatedVersions(createRefBook(), Arrays.asList(5L));

        assertEquals(1, relatedVersions.size());
        assertNotNull(relatedVersions.get(0));
    }

    @Test
    public void isVersionsExistReturnsTrueIfOneVersionExists() throws Exception {
        Date version = new GregorianCalendar(2011, 0, 1).getTime();

        boolean versionsExist = dao.isVersionsExist(createRefBook(), Arrays.asList(4L), version);

        assertEquals(true, versionsExist);
    }

    @Test
    public void isVersionsExistReturnsFalseIfNotExists() throws Exception {
        Date version = new GregorianCalendar(2009, 0, 1).getTime();

        boolean versionsExist = dao.isVersionsExist(createRefBook(), Arrays.asList(4L), version);

        assertEquals(false, versionsExist);
    }

    @Test
    public void updateRecordVersionUpdatesRecord() throws Exception {
        dao.updateRecordVersion(createRefBook(), 1L, getRecordsForUpdateRecordsVersion());

        Map<String, RefBookValue> updatedRecord = dao.getRecordData(createRefBook(), 1L);
        assertEquals("Майкл", updatedRecord.get("FIRST_NAME").getStringValue());
        assertEquals("Джексон", updatedRecord.get("LAST_NAME").getStringValue());
    }

    private Map<String, RefBookValue> getRecordsForUpdateRecordsVersion() {
        Map<String, RefBookValue> records = new HashMap<String, RefBookValue>();

        RefBookValue valueName = new RefBookValue(RefBookAttributeType.STRING, "Майкл");
        records.put("FIRST_NAME", valueName);

        RefBookValue valueLName = new RefBookValue(RefBookAttributeType.STRING, "Джексон");
        records.put("LAST_NAME", valueLName);

        return records;
    }

    @Test
    public void getFirstRecordIdReturnsId() throws Exception {
        Long firstRecordId = dao.getFirstRecordId(createRefBook(), 5L);

        assertEquals((Long)4L, firstRecordId);
    }

    @Test
    public void getFirstRecordIdReturnsNullIfNoRecordFound() throws Exception {
        Long firstRecordId = dao.getFirstRecordId(createRefBook(), 3L);

        assertEquals(null, firstRecordId);
    }

    @Test
    public void deleteAllRecordVersionsDeletes1Record() throws Exception {
        dao.deleteAllRecordVersions(createRefBook(), Arrays.asList(4L));

        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(createRefBook(), null, null, null, null, false);
        assertEquals(3, records.getTotalCount());
    }

    @Test
    public void deleteAllRecordVersionsDeletes2Records() throws Exception {
        dao.deleteAllRecordVersions(createRefBook(), Arrays.asList(3L, 4L));

        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(createRefBook(), null, null, null, null, false);
        assertEquals(2, records.getTotalCount());
    }
}
