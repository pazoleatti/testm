package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.test.BDUtilsMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.AssertTrue;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


//TODO: Необходимо добавить тесты для getRecords с фильтром (Marat Fayzullin 2013-08-31)

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "RefBookDaoTest.xml" })
@Transactional
public class RefBookDaoTest {

	public static final String ATTRIBUTE_PAGECOUNT = "order";
	public static final String ATTRIBUTE_AUTHOR = "author";
	public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_WEIGHT = "weight";
    private static final String REF_BOOK_RECORD_TABLE_NAME = "REF_BOOK_RECORD";

	@Autowired
    RefBookDao refBookDao;

    @Autowired
    private RefBookUtils refBookUtils;

    static Long cnt = 8L;

    @Before
    public void init(){
        ReflectionTestUtils.setField(refBookDao, "dbUtils", BDUtilsMock.getBDUtils());
    }

	@Test
	public void testGet1() {
		RefBook refBook1 = refBookDao.get(1L);
		assertEquals(1, refBook1.getId().longValue());
		assertEquals(4, refBook1.getAttributes().size());
	}

	@Test
	public void testGet2() {
		RefBook refBook2 = refBookDao.get(2L);
		assertEquals(2, refBook2.getId().longValue());
		assertEquals(1, refBook2.getAttributes().size());
	}

    @Test
    public void testGet3() {
        RefBook refBook3 = refBookDao.get(3L);
        assertEquals("24af57ef-ec1c-455f-a4fa-f0fb29483066", refBook3.getScriptId());
    }

	@Test
	public void testGetData1() throws Exception {
		RefBook refBook = refBookDao.get(1L);
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 1, 2013), null, null, refBook.getAttribute(ATTRIBUTE_NAME));
		// проверяем кол-во строк
		assertEquals(2, data.size());
		// проверяем типы значений
		for (int i = 0; i < 2; i++) {
			Map<String, RefBookValue> record = data.get(0);
			assertEquals(RefBookAttributeType.NUMBER, record.get(RefBook.RECORD_ID_ALIAS).getAttributeType());
			assertEquals(RefBookAttributeType.STRING, record.get(ATTRIBUTE_NAME).getAttributeType());
			assertEquals(RefBookAttributeType.NUMBER, record.get(ATTRIBUTE_PAGECOUNT).getAttributeType());
			assertEquals(RefBookAttributeType.REFERENCE, record.get(ATTRIBUTE_AUTHOR).getAttributeType());
		}
		sort(data);
		Map<String, RefBookValue> record = data.get(0);
		assertEquals(1, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		assertEquals("Алиса в стране чудес", record.get(ATTRIBUTE_NAME).getStringValue());
		assertEquals(1113, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
		assertEquals(5, record.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());

		Map<String, RefBookValue> record2 = data.get(1);
		assertEquals(4, record2.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		assertEquals("Вий", record2.get(ATTRIBUTE_NAME).getStringValue());
		assertEquals(425, record2.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
		assertEquals(6, record2.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
	}

	@Test
	public void testGetData2() throws Exception {
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 2, 2013), null, null, null);
		// проверяем кол-во строк
		assertEquals(2, data.size());
		sort(data);
		Map<String, RefBookValue> record = data.get(0);
		assertEquals(2, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		assertEquals("Алиса в стране", record.get(ATTRIBUTE_NAME).getStringValue());
		assertEquals(1213, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
		assertEquals(7, record.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
	}

	@Test
	public void testGetData3() throws Exception {
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 3, 2013), null, null, null);
        // проверяем кол-во строк
		assertEquals(2, data.size());
        Map<String, RefBookValue> record1 = data.get(0);
        assertEquals(2, record1.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
        assertEquals("Алиса в стране", record1.get(ATTRIBUTE_NAME).getStringValue());
        assertEquals(1213, record1.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
        assertEquals(7L, record1.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());

		Map<String, RefBookValue> record2 = data.get(1);
		assertEquals(4, record2.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		assertEquals("Вий", record2.get(ATTRIBUTE_NAME).getStringValue());
		assertEquals(425, record2.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
		assertEquals(6L, record2.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
	}

	@Test
	public void testGetData4() throws Exception {
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 1, 2012), null, null, null);
		// проверяем кол-во строк
		assertEquals(0, data.size());
	}

	@Test
	public void testGetData5() throws Exception {
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 1, 2014), null, null, null);
		// проверяем кол-во строк
		assertEquals(2, data.size());
	}

	/**
	 * Сортирует записи по коду
	 * @param data данные для сортировки
	 */
	private void sort(List<Map<String, RefBookValue>> data) {
		Collections.sort(data, new Comparator<Map<String, RefBookValue>>() {
			@Override
			public int compare(Map<String, RefBookValue> o1, Map<String, RefBookValue> o2) {
				Long l1 = o1.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
				Long l2 = o2.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
				return l1.compareTo(l2);
			}
		});
	}

	@Test
	public void testGetAll() {
		List<RefBook> refBooks = refBookDao.getAll(RefBookType.LINEAR.getId());
		assertEquals(3, refBooks.size());
		refBooks = refBookDao.getAll(RefBookType.HIERARCHICAL.getId());
		assertEquals(1, refBooks.size());
		refBooks = refBookDao.getAll(null);
		assertEquals(4, refBooks.size());
	}

	@Test
	public void testGetAllVisible() {
		assertEquals(2, refBookDao.getAllVisible(RefBookType.LINEAR.getId()).size());
		assertEquals(1, refBookDao.getAllVisible(RefBookType.HIERARCHICAL.getId()).size());
		assertEquals(3, refBookDao.getAllVisible(null).size());
	}

	@Test
	public void testGetRecordData(){
		Map<String, RefBookValue> record = refBookDao.getRecordData(1L, 4L);
		assertEquals(4, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		assertEquals("Вий", record.get(ATTRIBUTE_NAME).getStringValue());
		assertEquals(425, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
		assertEquals(6, record.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
	}

	private Date getDate(int day, int month, int year) {
		return new GregorianCalendar(year, month - 1, day, 15, 46, 57).getTime();
	}

	@Test
	public void testGetByAttribute1() {
		assertEquals(2, refBookDao.getByAttribute(4L).getId().longValue());
	}

	@Test
	public void testGetByAttribute2() {
		assertEquals(1, refBookDao.getByAttribute(3L).getId().longValue());
	}

	@Test(expected = DaoException.class)
	public void testGetByAttribute3() {
		refBookDao.getByAttribute(-123123L);
	}

	@Test
	public void testCreateRecords1() {
        RefBook refBook = refBookDao.get(1L);
        Date version = getDate(1, 1, 2012);

        List<RefBookRecord> records = new ArrayList<RefBookRecord>();
        int rowCount = 5;
        for (int i = 0; i < rowCount; i++) {
            RefBookRecord record = new RefBookRecord();
            Map<String, RefBookValue> values = refBook.createRecord();
            values.get(ATTRIBUTE_NAME).setValue("Название книги " + i);
            values.get(ATTRIBUTE_PAGECOUNT).setValue(100 + i);
            values.get(ATTRIBUTE_AUTHOR).setValue(6L);
            record.setRecordId((long) i);
            record.setValues(values);
            records.add(record);
        }
        refBookDao.createRecordVersion(refBook.getId(), version, VersionedObjectStatus.NORMAL, records);

        PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_NAME));
        assertEquals(rowCount, data.size());
        for (int i = 0; i < rowCount; i++) {
            Map<String, RefBookValue> record = data.get(i);
            assertEquals("Название книги " + i, record.get(ATTRIBUTE_NAME).getStringValue());
            assertEquals(100 + i, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().intValue());
            assertEquals(6, record.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
        }
    }

    @Test
    public void testCreateRecords2() {
        // Проверка округления
        RefBook refBook = refBookDao.get(1L);
        Date version = getDate(1, 1, 2012);
        List<RefBookRecord> records = new ArrayList<RefBookRecord>();

        Map<String, RefBookValue> values = refBook.createRecord();
        RefBookRecord record = new RefBookRecord();
        values.get(ATTRIBUTE_NAME).setValue("Название книги 1");
        values.get(ATTRIBUTE_PAGECOUNT).setValue(100);
        values.get(ATTRIBUTE_AUTHOR).setValue(6L);
        values.get(ATTRIBUTE_WEIGHT).setValue(0.9245);
        record.setValues(values);
        record.setRecordId(1L);
        records.add(record);

        values = refBook.createRecord();
        RefBookRecord record2 = new RefBookRecord();
        values.get(ATTRIBUTE_NAME).setValue("Название книги 2");
        values.get(ATTRIBUTE_PAGECOUNT).setValue(500);
        values.get(ATTRIBUTE_AUTHOR).setValue(6L);
        values.get(ATTRIBUTE_WEIGHT).setValue(2.1344);
        record2.setValues(values);
        record2.setRecordId(2L);
        records.add(record2);

        refBookDao.createRecordVersion(refBook.getId(), version, VersionedObjectStatus.NORMAL, records);

        PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBook.getId(), version,
                new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_WEIGHT));
        assertEquals(2, data.size());

        assertEquals(data.get(0).get(ATTRIBUTE_WEIGHT).getNumberValue().doubleValue(), 0.925d, 0d);
        assertEquals(data.get(1).get(ATTRIBUTE_WEIGHT).getNumberValue().doubleValue(), 2.134d, 0d);
    }

	/**
	 * Проверяем на пустом наборе данных
	 */
	@Test
	public void testCreateRecordsEmpty() {
		List<RefBookRecord> records = new ArrayList<RefBookRecord>();
		Date version = getDate(1, 3, 2013);
		refBookDao.createRecordVersion(1L, version, VersionedObjectStatus.NORMAL, records);
    }

	@Test
	public void testUpdateRecords1() {
		RefBook refBook = refBookDao.get(1L);
		Date version = getDate(1, 1, 2013);
		Date version2 = getDate(15, 1, 2013);

		// получаем данные для того, чтобы их изменить
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_NAME));
		Map<String, RefBookValue> record = data.get(1);
        record.get(ATTRIBUTE_NAME).setValue("Вий. Туда и обратно");
		record.get(ATTRIBUTE_PAGECOUNT).setValue(123);
		record.get(ATTRIBUTE_AUTHOR).setValue(null);
		// сохраняем изменения
        refBookDao.updateRecordVersion(refBook.getId(), record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), record);
        refBookUtils.updateVersionRelevancePeriod(REF_BOOK_RECORD_TABLE_NAME, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), version2);
		// проверяем изменения
		data = refBookDao.getRecords(refBook.getId(), version2, new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_NAME));
        assertEquals(data.size(), 2);
		record = data.get(1);
		assertEquals("Вий. Туда и обратно", record.get(ATTRIBUTE_NAME).getStringValue());
		assertEquals(123, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().intValue());
		Assert.assertNull(record.get(ATTRIBUTE_AUTHOR).getReferenceValue());
		// проверяем, что версия поменялась
		data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_NAME));
        assertEquals(data.size(), 1);
	}

    @Test
    public void testUpdateRecords2() {
        // Проверка обновления при совпадении версий
        Long refBookId = 1L;
        // Существующая дата
        Date version = getDate(1, 1, 2013);
        PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBookId, version, new PagingParams(), null, null);
        Map<String, RefBookValue> record = data.get(1);

        record.get(RefBookDaoTest.ATTRIBUTE_NAME).setValue("Вий. Туда и обратно");
        record.get(ATTRIBUTE_PAGECOUNT).setValue(123);
        record.get(RefBookDaoTest.ATTRIBUTE_AUTHOR).setValue(7L);
        refBookDao.updateRecordVersion(refBookId, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), record);

        data = refBookDao.getRecords(refBookId, version, new PagingParams(), null, null);
        record = data.get(1);
        assertEquals(record.get(RefBookDaoTest.ATTRIBUTE_NAME).getStringValue(), "Вий. Туда и обратно");
        assertEquals(record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().longValue(), 123L);
        assertEquals(record.get(RefBookDaoTest.ATTRIBUTE_AUTHOR).getReferenceValue(), Long.valueOf(7L));
    }

    @Test
    public void testUpdateRecords3() {
        // Проверка округления
        Long refBookId = 1L;
        // Существующая дата
        Date version = getDate(1, 1, 2013);
        PagingParams pp = new PagingParams();
        pp.setCount(3);
        PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBookId, version, pp, null, null);

        Map<String, RefBookValue> record = data.get(0);
        record.get(ATTRIBUTE_WEIGHT).setValue(0.123456789d);
        refBookDao.updateRecordVersion(refBookId, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), record);

        record = data.get(1);
        record.get(ATTRIBUTE_WEIGHT).setValue(-345.9905);
        refBookDao.updateRecordVersion(refBookId, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), record);

        data = refBookDao.getRecords(refBookId, version, new PagingParams(), null, null);

        assertEquals(data.get(0).get(ATTRIBUTE_WEIGHT).getNumberValue().doubleValue(),
                0.123d, 0d);
        assertEquals(data.get(1).get(ATTRIBUTE_WEIGHT).getNumberValue().doubleValue(),
                -345.991d, 0d);
    }

    @Test
    public void checkRecordUnique() {
       assertEquals(refBookDao.checkRecordUnique(1L, getDate(1, 1, 2013), 1L), Long.valueOf(1L));
       Assert.assertNull(refBookDao.checkRecordUnique(1L, getDate(2, 1, 2013), 1L));
    }

    @Test
    public void testGetValue1() {
        // Cуществующие значения
        assertEquals(refBookDao.getValue(1L, 1L).getStringValue(), "Алиса в стране чудес");
        assertEquals(refBookDao.getValue(1L, 2L).getNumberValue().intValue(), 1113);
    }

    @Test(expected = DaoException.class)
    public void testGetValue2() {
        // Не существующее значение
        refBookDao.getValue(-1L, 2L);
    }

    @Test
    public void testUseForignStatements(){
        Date version = getDate(1, 2, 2013);                                                                                 // order is null and
        PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, version, new PagingParams(), "author.name like 'Петренко%'", null);
        assertTrue(data.size() == 1);
    }

    @Test
    public void getRecordVersion(){
        RefBookRecordVersion info = refBookDao.getRecordVersionInfo(1L);
        assertEquals(info.getRecordId().longValue(), 1L);
        assertEquals(getZeroTimeDate(getDate(1, 1, 2013)), getZeroTimeDate(info.getVersionStart()));
        assertEquals(getZeroTimeDate(getDate(31, 1, 2013)), getZeroTimeDate(info.getVersionEnd()));
    }

    @Test(expected = DaoException.class)
    public void getActiveRecordVersion(){
        refBookDao.getRecordVersionInfo(-1L);
    }

    @Test
    public void getRecordVersionCount(){
        int count = refBookDao.getRecordVersionsCount(2L, 6L);
        assertTrue(count == 2);

        count = refBookDao.getRecordVersionsCount(1L, 4L);
        assertTrue(count == 1);

        count = refBookDao.getRecordVersionsCount(5L, 1L);
        assertTrue(count == 0);
    }

    @Test
    public void getUniqueAttributeValues() {
        List<Pair<RefBookAttribute, RefBookValue>> values = refBookDao.getUniqueAttributeValues(1L, 1L);
        assertEquals(1, values.size());
        assertEquals("Алиса в стране чудес", values.get(0).getSecond().getStringValue());
    }

    @Test
    public void getMatchedRecordsByUniqueAttributes() {
        RefBook refBook = refBookDao.get(1L);
        PagingResult<Map<String, RefBookValue>> allValues = refBookDao.getRecords(refBook.getId(), getDate(1, 1, 2013), null, null, null);
        assertEquals(2, allValues.size());
        List<RefBookRecord> records = new ArrayList<RefBookRecord>();
        for (Map<String, RefBookValue> values : allValues) {
            RefBookRecord record = new RefBookRecord();
            record.setValues(values);
            record.setRecordId(null);
            records.add(record);
        }
        List<Pair<Long,String>> matches = refBookDao.getMatchedRecordsByUniqueAttributes(refBook.getId(), refBook.getAttributes(), records);
        assertEquals(2, matches.size());
    }

    @Test
    public void checkReferenceValuesVersions() {
        RefBook refBook = refBookDao.get(1L);
        PagingResult<Map<String, RefBookValue>> allValues = refBookDao.getRecords(refBook.getId(), getDate(1, 1, 2013), null, null, null);
        assertEquals(2, allValues.size());
        List<RefBookRecord> records = new ArrayList<RefBookRecord>();
        for (Map<String, RefBookValue> values : allValues) {
            RefBookRecord record = new RefBookRecord();
            record.setValues(values);
            record.setRecordId(null);
            records.add(record);
        }
        boolean isOk = refBookUtils.isReferenceValuesCorrect(REF_BOOK_RECORD_TABLE_NAME, getDate(1, 1, 2011), refBook.getAttributes(), records);
        assertTrue(isOk);
        isOk = refBookUtils.isReferenceValuesCorrect(REF_BOOK_RECORD_TABLE_NAME, new Date(), refBook.getAttributes(), records);
        assertFalse(isOk);
    }

    @Test
    public void checkVersionUsages() {
        List<String> results = refBookDao.isVersionUsed(1L, Arrays.asList(1L));
        assertEquals(true, !results.isEmpty());

        boolean isOk = !refBookDao.isVersionUsed(1L, 1L, getDate(1, 1, 2013));
        assertEquals(true, isOk);
    }

    @Test
    public void deleteAllRecordVersions() {
        PagingResult<Map<String, RefBookValue>> records = refBookDao.getRecords(1L, getDate(1, 1, 2014), null, null, null);
        assertEquals(2, records.size());
        refBookDao.deleteAllRecordVersions(1L, Arrays.asList(2L, 4L));
        records = refBookDao.getRecords(1L, getDate(1, 1, 2014), null, null, null);
        assertEquals(0, records.size());
    }

    @Test
    public void deleteRecordVersions() {
        PagingResult<Map<String, RefBookValue>> records = refBookDao.getRecords(1L, getDate(1, 1, 2013), null, null, null);
        assertEquals(2, records.size());
        refBookUtils.deleteRecordVersions(REF_BOOK_RECORD_TABLE_NAME, Arrays.asList(1L));
        records = refBookDao.getRecords(1L, getDate(1, 1, 2013), null, null, null);
        assertEquals(1, records.size());
    }

    @Test
    public void isVersionExist() {
        assertTrue(refBookDao.isVersionsExist(1L, Arrays.asList(1L), getDate(1, 1, 2013)));
        assertFalse(refBookDao.isVersionsExist(1L, Arrays.asList(1L), getDate(1, 1, 2014)));
    }

    @Test
    public void getRecordsVersionStart() {
        Map<Long, Date> result = refBookDao.getRecordsVersionStart(Arrays.asList(1L, 2L));
        assertEquals(getZeroTimeDate(getDate(1, 1, 2013)), result.get(1L));
        assertEquals(getZeroTimeDate(getDate(1, 2, 2013)), result.get(2L));
    }

    private static Date getZeroTimeDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

	private static final String PARENT_FILTER_1 = RefBook.RECORD_PARENT_ID_ALIAS + " = 1";
	private static final String PARENT_FILTER_NULL = RefBook.RECORD_PARENT_ID_ALIAS + " is null";

	@Test
	public void getParentFilterTest1() {
		Assert.assertEquals(PARENT_FILTER_1, RefBookDaoImpl.getParentFilter("", 1L));
	}

	@Test
	public void getParentFilterTest2() {
		final String filter = "NAME = 'sample'";
		Assert.assertEquals(filter + " AND " + PARENT_FILTER_1, RefBookDaoImpl.getParentFilter(filter, 1L));
	}

	@Test
	public void getParentFilterTest3() {
		final String filter = "NAME = 'sample'";
		Assert.assertEquals(filter + " AND " + PARENT_FILTER_NULL, RefBookDaoImpl.getParentFilter(filter, null));
	}

	private static final Long OKATO_REF_BOOK_ID = 4L;

	@Test
	public void checkHierarchicalTest1() {
		RefBook refBook = refBookDao.get(OKATO_REF_BOOK_ID);
		Assert.assertTrue(RefBookDaoImpl.checkHierarchical(refBook));
	}

	@Test
	public void checkHierarchicalTest2() {
		Assert.assertFalse(RefBookDaoImpl.checkHierarchical(refBookDao.get(1L)));
		Assert.assertFalse(RefBookDaoImpl.checkHierarchical(refBookDao.get(2L)));
		Assert.assertFalse(RefBookDaoImpl.checkHierarchical(refBookDao.get(3L)));
	}

	private void checkChildrenCount(Long parentId, int expectedCount) {
		List<Map<String, RefBookValue>> data = refBookDao.getChildrenRecords(OKATO_REF_BOOK_ID, parentId, getDate(1, 1, 2013), null, null, null);
		Assert.assertEquals(expectedCount, data.size());
	}

	@Test
	public void getChildrenRecordsTest() {
		checkChildrenCount(null, 4);
		checkChildrenCount(8L, 2);
		checkChildrenCount(9L, 0);
		checkChildrenCount(10L, 1);
		checkChildrenCount(11L, 0);
		checkChildrenCount(12L, 3);
		checkChildrenCount(13L, 0);
		checkChildrenCount(14L, 0);
		checkChildrenCount(15L, 0);
		checkChildrenCount(16L, 0);
		checkChildrenCount(17L, 0);
	}

    @Test
    public void hasChildren() {
        List<Date> versions = refBookDao.hasChildren(4L, Arrays.asList(8L));
        assertEquals(1, versions.size());
        assertEquals(getZeroTimeDate(getDate(1,1,2013)), getZeroTimeDate(versions.get(0)));
    }

}
