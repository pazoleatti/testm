package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookSimpleDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Ignore // Отключил т.к. все тесты построены на справочнике ФЛ. А теперь это реестр ФЛ.
public class RefBookSimpleDaoTest {

    private static final int TABLE_TOTAL_RECORDS = 5;
    private static final String REF_BOOK_TABLE_NAME = "REF_BOOK_PERSON";
    private static final Long REF_BOOK_ID = 904L;

    @Autowired
    private RefBookSimpleDaoImpl dao;
    @Autowired
    private RefBookDao refBookDao;

    @Test
    public void getRecordsReturnsAllRecords() {
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, null, null, null, true);

        assertEquals(TABLE_TOTAL_RECORDS, data.getTotalCount());
        assertEquals(TABLE_TOTAL_RECORDS, data.size());
    }

    @Test
    public void getRecordsReturnsVersion1() {
        Date version = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime();
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), version, null, null, null, true);

        assertEquals(2, data.size());
    }

    @Test
    public void getRecordsReturnsVersion2() {
        Date version = new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime();
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), version, null, null, null, true);

        assertEquals(4, data.size());
    }

    @Test
    public void getRecordsReturnsLastVersion() {
        Date version = new GregorianCalendar(2011, Calendar.JULY, 1).getTime();

        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), version, null, null, null, true);

        assertEquals(1, data.size());
        Date expectedBirthday = new GregorianCalendar(1948, 1, 8).getTime();
        assertEquals(expectedBirthday, data.get(0).get("BIRTH_DATE").getDateValue());
    }

    @Test
    public void getRecordsReturnsPaginated0Count3() {
        PagingParams pagingParams = new PagingParams(0, 3);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, pagingParams, null, null, true);

        assertEquals(3, data.size());
    }

    @Test
    public void getRecordsReturnsPaginated2Count2() {
        PagingParams pagingParams = new PagingParams(2, 2);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, pagingParams, null, null, true);

        assertEquals(2, data.size());
    }

    @Test
    public void getRecordsReturnsEmptyOnWrongPageParams() {
        PagingParams pagingParams = new PagingParams(99, 9);
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, pagingParams, null, null, true);

        assertEquals(0, data.size());
    }

    @Test
    public void getRecordsReturnsFiltered() {
        String filter = "MIDDLE_NAME = 'Васильевич'";
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, null, filter, null, true);

        assertEquals(2, data.size());
        assertEquals("Васильевич", data.get(0).get("MIDDLE_NAME").getStringValue());
        assertEquals("Васильевич", data.get(1).get("MIDDLE_NAME").getStringValue());
    }

    @Test
    public void getRecordsReturnsSorted() {
        RefBookAttribute sortAttribute = new RefBookAttribute();
        sortAttribute.setAlias("FIRST_NAME");
        PagingResult<Map<String, RefBookValue>> data = dao.getRecords(createRefBook(), null, null, null, sortAttribute, true);
        PagingResult<Map<String, RefBookValue>> dataDescending = dao.getRecords(createRefBook(), null, null, null, sortAttribute, false);

        assertEquals("Петр", data.get(0).get("FIRST_NAME").getStringValue());
        assertEquals("Феофан", dataDescending.get(0).get("FIRST_NAME").getStringValue());
    }

    @Test
    public void getUniqueRecordIdsReturnsIds() {
        List<Long> ids = dao.getUniqueRecordIds(createRefBook(), null, null);

        assertEquals(TABLE_TOTAL_RECORDS, ids.size());
    }

    @Test
    public void getUniqueRecordIdsReturnsIdsWithVersionSet() {
        Date version = new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime();
        List<Long> ids = dao.getUniqueRecordIds(createRefBook(), version, null);

        assertEquals(2, ids.size());
    }

    @Test
    public void getRecordsCountReturnsAllRecordsCount() {
        int result = dao.getRecordsCount(createRefBook(), null, null);

        assertEquals(TABLE_TOTAL_RECORDS, result);
    }

    @Test
    public void getRecordsCountReturnsVersionedRecordsCount() {
        Date version = new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime();
        int result = dao.getRecordsCount(createRefBook(), version, null);

        assertEquals(4, result);
    }

    @Test
    public void getRecordVersionInfoReturnsVersionInfo() {
        Date expectedVersionStart = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
        Date expectedVersionEnd = new GregorianCalendar(2010, Calendar.DECEMBER, 31).getTime();

        RefBookRecordVersion versionInfo = dao.getRecordVersionInfo(createRefBook(), 4L);

        assertEquals((Long) 4L, versionInfo.getRecordId());
        assertEquals(expectedVersionStart, versionInfo.getVersionStart());
        assertEquals(expectedVersionEnd, versionInfo.getVersionEnd());
        assertFalse(versionInfo.isVersionEndFake());
    }

    @Test
    public void getRecordIdReturnsId() {
        Long expect3 = dao.getRecordId(createRefBook(), 3L);
        Long expect4 = dao.getRecordId(createRefBook(), 5L);

        assertEquals((Long) 3L, expect3);
        assertEquals((Long) 4L, expect4);
    }

    @Test(expected = DaoException.class)
    public void getRecordThrowsIfNotFound() {
        dao.getRecordId(createRefBook(), 99L);
    }

    @Test
    public void getRecordVersionsByRecordIdReturnsVersions() {
        Date d1948 = new GregorianCalendar(1948, Calendar.FEBRUARY, 8).getTime();
        Date d1946 = new GregorianCalendar(1946, Calendar.FEBRUARY, 8).getTime();
        List<Date> dates = new ArrayList<>();
        dates.add(d1946);
        dates.add(d1948);

        PagingResult<Map<String, RefBookValue>> data = dao.getRecordVersionsByRecordId(createRefBook(), 4L, null, null, null);

        assertEquals(2, data.size());
        for (Map<String, RefBookValue> refBook : data) {
            assertTrue(dates.contains(refBook.get("BIRTH_DATE").getDateValue()));
        }
    }

    @Test
    public void getRecordVersionsByRecordIdReturnsFiltered() {
        Date d1948 = new GregorianCalendar(1948, Calendar.FEBRUARY, 8).getTime();

        PagingResult<Map<String, RefBookValue>> data = dao.getRecordVersionsByRecordId(createRefBook(), 4L, null, "BIRTH_PLACE = 'Калуга'", null);

        assertEquals(1, data.size());
        assertEquals(d1948, data.get(0).get("BIRTH_DATE").getDateValue());

    }

    @Test
    public void getMatchedRecordsByUniqueAttributesFindsRecord1() {
        RefBook refBook = createRefBook();
        List<Pair<Long, String>> matchedRecords = dao.getMatchedRecordsByUniqueAttributes(refBook, null, createRecords().get(0));

        assertEquals(1, matchedRecords.size());
        assertEquals((Long)1L, matchedRecords.get(0).getFirst());

        System.out.println(matchedRecords);
    }

    @Test
    public void getMatchedRecordsByUniqueAttributesFindsRecord2() {
        RefBook refBook = createRefBook();
        List<Pair<Long, String>> matchedRecords = dao.getMatchedRecordsByUniqueAttributes(refBook, 4L, createRecords().get(1));

        assertEquals(1, matchedRecords.size());
        assertEquals((Long)2L, matchedRecords.get(0).getFirst());
    }

    @Test
    public void getMatchedRecordsByUniqueAttributesNotFindsRecord() {
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

        return new ArrayList<>(Arrays.asList(record, record2));
    }

    @Test
    public void checkConflictValuesVersionsReturnsConflict() {
        List<Long> conflicts = dao.checkConflictValuesVersions(createRefBook(), createValues(), new Date(0), new Date());
        assertEquals(1, conflicts.size());
        assertEquals((Long)1L, conflicts.get(0));
    }

    private List<Pair<Long, String>> createValues(){
        Pair<Long, String> p1 = new Pair<>(1L, "ИНН в Российской Федерации,ИНН в стране гражданства,СНИЛС");
        return new ArrayList<>(Collections.singletonList(p1));
    }

    @Test
    public void checkConflictValuesVersionsReturnsNoConflict() {
        Date d2013 = new GregorianCalendar(2013,0,1).getTime();
        Date d2015 = new GregorianCalendar(2015,0,1).getTime();
        List<Long> conflicts = dao.checkConflictValuesVersions(createRefBook(), createValues(), d2013, d2015);
        assertEquals(0, conflicts.size());
    }

    @Test
    public void checkCrossVersionsExecutes() {
        List<CheckCrossVersionsResult> resultList = dao.checkCrossVersions(createRefBook(), 4L, new Date(0), new Date(), null);
        assertNotNull(resultList);
    }

    @Test
    public void getNextVersionExecutes() {
        dao.getNextVersion(createRefBook(), new Date(0), "TAXPAYER_STATE = 1");
    }

    @Test
    public void getNextVersionReturnsNextVersion() {
        RefBookRecordVersion nextVersion = dao.getNextVersion(createRefBook(), 1L, new Date(0));
        Date expectedStartDate = new GregorianCalendar(2017, 0, 9).getTime();

        assertEquals(expectedStartDate, nextVersion.getVersionStart());
        assertNull(nextVersion.getVersionEnd());
        assertEquals((Long)1L, nextVersion.getRecordId());
    }

    @Test
    public void getNextVersionReturnsNextVersion2() {
        RefBookRecordVersion nextVersion = dao.getNextVersion(createRefBook(), 4L, new Date(0));
        Date expectedStartDate = new GregorianCalendar(2010, 0, 1).getTime();
        Date expectedEndDate = new GregorianCalendar(2010, 11, 31).getTime();

        assertEquals(expectedStartDate, nextVersion.getVersionStart());
        assertEquals(expectedEndDate, nextVersion.getVersionEnd());
        assertEquals((Long)4L, nextVersion.getRecordId());
    }

    @Test
    public void getNextVersionReturnsNextVersion3() {
        Date versionFrom = new GregorianCalendar(2010,6,22).getTime();
        RefBookRecordVersion nextVersion = dao.getNextVersion(createRefBook(), 4L, versionFrom);
        Date expectedStartDate = new GregorianCalendar(2011, 0, 1).getTime();

        assertEquals(expectedStartDate, nextVersion.getVersionStart());
        assertNull(nextVersion.getVersionEnd());
        assertEquals((Long)5L, nextVersion.getRecordId());
    }

    @Test
    public void getPreviousVersionReturnsPrevVersion() {
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
    public void createFakeRecordVersionInsertsData() {
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
    public void getRecordDataReturnsData() {
        Date expectedBirthday = new GregorianCalendar(1948,1,8).getTime();

        Map<String, RefBookValue> recordData = dao.getRecordData(createRefBook(), 5L);

        assertEquals(expectedBirthday, recordData.get("BIRTH_DATE").getDateValue());
        assertEquals("Васильевич", recordData.get("MIDDLE_NAME").getStringValue());
        assertEquals("Калуга", recordData.get("BIRTH_PLACE").getStringValue());
    }

    @Test
    public void getRecordData2ReturnsData() {
        Map<Long, Map<String, RefBookValue>> recordData = dao.getRecordData(createRefBook(), new ArrayList<>(Arrays.asList(1L, 2L)));

        assertEquals(2, recordData.size());
        assertEquals("Федор", recordData.get(1L).get("FIRST_NAME").getStringValue());
        assertEquals("Петр", recordData.get(2L).get("FIRST_NAME").getStringValue());
    }

    @Test
    public void findRecordReturnsId() {
        Date version = new GregorianCalendar(2011, 0, 1).getTime();

        Long id = dao.findRecord(createRefBook(), 4L, version);

        assertEquals((Long)5L, id);
    }

    @Test
    public void getRelatedVersionsReturnsId() {
        Date fakeVersion = new GregorianCalendar(2020,5,5).getTime();
        dao.createFakeRecordVersion(createRefBook(), 4L, fakeVersion);

        List<Long> relatedVersions = dao.getRelatedVersions(createRefBook(), Collections.singletonList(5L));

        assertEquals(1, relatedVersions.size());
        assertNotNull(relatedVersions.get(0));
    }

    @Test
    public void isVersionsExistReturnsTrueIfOneVersionExists() {
        Date version = new GregorianCalendar(2011, 0, 1).getTime();

        boolean versionsExist = dao.isVersionsExist(createRefBook(), Collections.singletonList(4L), version);

        assertTrue(versionsExist);
    }

    @Test
    public void isVersionsExistReturnsFalseIfNotExists() {
        Date version = new GregorianCalendar(2009, 0, 1).getTime();

        boolean versionsExist = dao.isVersionsExist(createRefBook(), Collections.singletonList(4L), version);

        assertFalse(versionsExist);
    }

    @Test
    public void updateRecordVersionUpdatesRecord() {
        dao.updateRecordVersion(createRefBook(), 1L, getRecordsForUpdateRecordsVersion());

        Map<String, RefBookValue> updatedRecord = dao.getRecordData(createRefBook(), 1L);
        assertEquals("Майкл", updatedRecord.get("FIRST_NAME").getStringValue());
        assertEquals("Джексон", updatedRecord.get("LAST_NAME").getStringValue());
    }

    private Map<String, RefBookValue> getRecordsForUpdateRecordsVersion() {
        Map<String, RefBookValue> records = new HashMap<>();

        RefBookValue valueName = new RefBookValue(RefBookAttributeType.STRING, "Майкл");
        records.put("FIRST_NAME", valueName);

        RefBookValue valueLName = new RefBookValue(RefBookAttributeType.STRING, "Джексон");
        records.put("LAST_NAME", valueLName);

        return records;
    }

    @Test
    public void deleteAllRecordVersionsDeletes1Record() {
        dao.deleteAllRecordVersions(createRefBook(), Collections.singletonList(4L));

        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(createRefBook(), null, null, null, null, false);
        assertEquals(3, records.getTotalCount());
    }

    @Test
    public void deleteAllRecordVersionsDeletes2Records() {
        dao.deleteAllRecordVersions(createRefBook(), Arrays.asList(3L, 4L));

        PagingResult<Map<String, RefBookValue>> records = dao.getRecords(createRefBook(), null, null, null, null, false);
        assertEquals(2, records.getTotalCount());
    }

	private Map<String, RefBookValue> findRecord(PagingResult<Map<String, RefBookValue>> list, @NotNull String a, @NotNull String b) {
		for (Map<String, RefBookValue> rec : list) {
			if (a.equals(rec.get("a").getStringValue()) && b.equals(rec.get("b").getStringValue())) {
				return rec;
			}
		}
		return null;
	}

	public static Date getDate(int year, int month, int day) {
		Calendar date = Calendar.getInstance();
		date.clear();
		date.set(year, month, day);
		return date.getTime();
	}

	// Тестируем все случаи отрезков
	@Test
	public void getRecordsPeriod() {
		RefBook refBook = refBookDao.get(10000L);
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(2016, Calendar.JUNE, 1); Date dateFrom = cal.getTime();
		cal.set(2016, Calendar.SEPTEMBER, 1); Date dateTo = cal.getTime();
		PagingResult<Map<String, RefBookValue>> list = dao.getVersionsInPeriod(refBook, dateFrom, dateTo, null);

		int[][] trueList =
				{{2,1},{3,2},{4,1},{5,2},{6,1},{7,2},{8,1},{9,2},{10,1},{10,2},
				{11,2},{11,3},{12,1},{13,2},{14,1},{14,2},{15,2},{15,3},{16,1},{16,2},
				{17,2},{17,3},{18,1},{18,2},{18,3},{19,2},{19,3},{19,4},{20,1},{21,2},
				{22,1},{22,2},{23,2},{23,3},{24,1},{24,2},{25,2},{25,3},{26,1},{26,2},
				{26,3},{27,2},{27,3},{27,4},{30,1},{31,2},{32,1},{33,2},{34,1},{35,2},
				{36,1},{36,2},{37,2},{37,3},{38,1},{39,2},{40,1},{40,2},{41,2},{41,3},
				{42,1},{42,2},{43,2},{43,3},{44,1},{44,2},{44,3},{45,2},{45,3},{45,4},
				{48,1},{49,2},{50,1},{51,2},{52,1},{53,2},{54,1},{54,2},{55,2},{55,3},
				{56,1},{57,2},{58,1},{58,2},{59,2},{59,3},{60,1},{60,2},{61,2},{61,3},
				{62,1},{62,2},{62,3},{63,2},{63,3},{63,4}};

		int[][] falseList =
				{{1, 1},{1, 9},{2, 9},{3, 1},{3, 9},{4, 9},{5, 1},{5, 9},{7, 1},{8, 9},
				{9, 1},{9, 9},{9, 9},{10, 9},{11, 1},{11, 9},{12, 9},{13, 1},{13, 9},{13, 9},
				{14, 9},{14, 9},{15, 1},{15, 9},{15, 9},{16, 9},{17, 1},{17, 9},{17, 9},{18, 9},
				{19, 1},{19, 9},{21, 1},{21, 9},{22, 9},{23, 1},{23, 9},{25, 1},{25, 9},{27, 1},
				{28, 1},{28, 9},{29, 1},{29, 9},{29, 1},{29, 9},{30, 9},{30, 2},{30, 9},{31, 1},
				{31, 9},{31, 3},{31, 9},{32, 2},{32, 9},{33, 1},{33, 3},{33, 9},{34, 9},{34, 2},
				{34, 9},{35, 1},{35, 9},{35, 9},{35, 3},{35, 9},{36, 9},{36, 3},{36, 9},{37, 1},
				{37, 9},{37, 4},{37, 9},{38, 2},{38, 9},{39, 1},{39, 9},{39, 3},{39, 9},{40, 9},
				{40, 3},{40, 9},{41, 1},{41, 9},{41, 4},{41, 9},{42, 3},{42, 9},{43, 1},{43, 9},
				{43, 4},{43, 9},{44, 4},{44, 9},{45, 1},{45, 5},{46, 1},{46, 9},{46, 2},{47, 1},
				{47, 9},{47, 1},{48, 9},{48, 2},{49, 1},{49, 9},{49, 3},{50, 2},{51, 1},{51, 3},
				{52, 9},{52, 2},{53, 1},{53, 9},{53, 9},{53, 3},{54, 9},{54, 3},{55, 1},{55, 9},
				{55, 4},{56, 2},{57, 1},{57, 9},{57, 3},{58, 9},{58, 3},{59, 1},{59, 9},{59, 4},
				{60, 3},{61, 1},{61, 9},{61, 4},{62, 4},{63, 1}, {63, 5}};

		for (int[] a : trueList) {
			assertNotNull(findRecord(list, String.valueOf(a[0]), String.valueOf(a[1])));
		}
		for (int[] a : falseList) {
			assertNull(findRecord(list, String.valueOf(a[0]), String.valueOf(a[1])));
		}

		Map<String, RefBookValue> rec = findRecord(list, "26", "1");
		assertEquals(26L, rec.get(RefBook.BUSINESS_ID_ALIAS).getNumberValue());
		assertEquals(getDate(2016, 4, 1), rec.get(RefBook.RECORD_VERSION_FROM_ALIAS).getDateValue());
		assertEquals(getDate(2016, 6, 1), rec.get(RefBook.RECORD_VERSION_TO_ALIAS).getDateValue());

		rec = findRecord(list, "45", "4");
		assertEquals(45L, rec.get(RefBook.BUSINESS_ID_ALIAS).getNumberValue());
		assertEquals(getDate(2016, 7, 1), rec.get(RefBook.RECORD_VERSION_FROM_ALIAS).getDateValue());
		assertEquals(getDate(2016, 9, 1), rec.get(RefBook.RECORD_VERSION_TO_ALIAS).getDateValue());

		assertNotNull(findRecord(list, "64", "1"));
		assertNotNull(findRecord(list, "64", "1"));
	}
}