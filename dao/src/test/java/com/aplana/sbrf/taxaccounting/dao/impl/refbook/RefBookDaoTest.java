package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.DaoException;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "RefBookDaoTest.xml" })
@Transactional
public class RefBookDaoTest {

	public static final String ATTRIBUTE_PAGECOUNT = "order";
	public static final String ATTRIBUTE_AUTHOR = "author";
	public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_WEIGHT = "weight";

	@Autowired
    RefBookDao refBookDao;

	@Test
	public void testGet1() {
		RefBook refBook1 = refBookDao.get(1L);
		Assert.assertEquals(1, refBook1.getId().longValue());
		Assert.assertEquals(4, refBook1.getAttributes().size());
	}

	@Test
	public void testGet2() {
		RefBook refBook2 = refBookDao.get(2L);
		Assert.assertEquals(2, refBook2.getId().longValue());
		Assert.assertEquals(1, refBook2.getAttributes().size());
	}

    @Test
    public void testGet3() {
        RefBook refBook3 = refBookDao.get(3L);
        Assert.assertEquals("24af57ef-ec1c-455f-a4fa-f0fb29483066", refBook3.getScriptId());
    }

	@Test
	public void testGetData1() throws Exception {
		RefBook refBook = refBookDao.get(1L);
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 1, 2013), null, null, refBook.getAttribute(ATTRIBUTE_NAME));
		System.out.println(data);
		// проверяем кол-во строк
		Assert.assertEquals(2, data.getRecords().size());
		// проверяем типы значений
		for (int i = 0; i < 2; i++) {
			Map<String, RefBookValue> record = data.getRecords().get(0);
			Assert.assertEquals(RefBookAttributeType.NUMBER, record.get(RefBook.RECORD_ID_ALIAS).getAttributeType());
			Assert.assertEquals(RefBookAttributeType.STRING, record.get(ATTRIBUTE_NAME).getAttributeType());
			Assert.assertEquals(RefBookAttributeType.NUMBER, record.get(ATTRIBUTE_PAGECOUNT).getAttributeType());
			Assert.assertEquals(RefBookAttributeType.REFERENCE, record.get(ATTRIBUTE_AUTHOR).getAttributeType());
		}
		sort(data.getRecords());
		Map<String, RefBookValue> record = data.getRecords().get(0);
		Assert.assertEquals(1, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		Assert.assertEquals("Алиса в стране чудес", record.get(ATTRIBUTE_NAME).getStringValue());
		Assert.assertEquals(1113, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
		Assert.assertEquals(5, record.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());

		Map<String, RefBookValue> record2 = data.getRecords().get(1);
		Assert.assertEquals(4, record2.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		Assert.assertEquals("Вий", record2.get(ATTRIBUTE_NAME).getStringValue());
		Assert.assertEquals(425, record2.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
		Assert.assertEquals(6, record2.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
	}

	@Test
	public void testGetData2() throws Exception {
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 2, 2013), null, null, null);
		System.out.println(data);
		// проверяем кол-во строк
		Assert.assertEquals(2, data.getRecords().size());
		sort(data.getRecords());
		Map<String, RefBookValue> record = data.getRecords().get(0);
		Assert.assertEquals(2, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		Assert.assertEquals("Алиса в стране", record.get(ATTRIBUTE_NAME).getStringValue());
		Assert.assertEquals(1213, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
		Assert.assertEquals(7, record.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
	}

	@Test
	public void testGetData3() throws Exception {
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 3, 2013), null, null, null);
		System.out.println(data);
		// проверяем кол-во строк
		Assert.assertEquals(1, data.getRecords().size());
		Map<String, RefBookValue> record2 = data.getRecords().get(0);
		Assert.assertEquals(4, record2.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		Assert.assertEquals("Вий", record2.get(ATTRIBUTE_NAME).getStringValue());
		Assert.assertEquals(425, record2.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
		Assert.assertEquals(6L, record2.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
	}

	@Test
	public void testGetData4() throws Exception {
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 1, 2012), null, null, null);
		System.out.println(data);
		// проверяем кол-во строк
		Assert.assertEquals(0, data.getRecords().size());
	}

	@Test
	public void testGetData5() throws Exception {
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 1, 2014), null, null, null);
		System.out.println(data);
		// проверяем кол-во строк
		Assert.assertEquals(1, data.getRecords().size());
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
		List<RefBook> refBooks = refBookDao.getAll();
		Assert.assertEquals(3, refBooks.size());
	}

	@Test
	public void testGetRecordData(){
		Map<String, RefBookValue> record = refBookDao.getRecordData(1L, 4L);
		Assert.assertEquals(4, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		Assert.assertEquals("Вий", record.get(ATTRIBUTE_NAME).getStringValue());
		Assert.assertEquals(425, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().doubleValue(), 1e-5);
		Assert.assertEquals(6, record.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
	}

	@Test
	public void testGetVersions1() throws Exception {
		List<Date> versions = refBookDao.getVersions(1L, getDate(1, 1, 2013), getDate(1, 1, 2014));
		Assert.assertEquals(3, versions.size());
	}

	@Test
	public void testGetVersions2() throws Exception {
		List<Date> versions = refBookDao.getVersions(2L, getDate(1, 1, 2013), getDate(1, 1, 2014));
		Assert.assertEquals(2, versions.size());
	}

	@Test
	public void testGetVersions3() throws Exception {
		List<Date> versions = refBookDao.getVersions(1L, getDate(1, 1, 2013), getDate(1, 2, 2013));
		Assert.assertEquals(2, versions.size());
	}

	@Test
	public void testGetVersions4() throws Exception {
		List<Date> versions = refBookDao.getVersions(1L, getDate(1, 1, 2020), getDate(1, 1, 2030));
		Assert.assertEquals(1, versions.size());
	}

	private Date getDate(int day, int month, int year) {
		return new GregorianCalendar(year, month - 1, day, 15, 46, 57).getTime();
	}

	@Test
	public void testGetByAttribute1() {
		Assert.assertEquals(2, refBookDao.getByAttribute(4).getId().longValue());
	}

	@Test
	public void testGetByAttribute2() {
		Assert.assertEquals(1, refBookDao.getByAttribute(3).getId().longValue());
	}

	@Test(expected = DaoException.class)
	public void testGetByAttribute3() {
		refBookDao.getByAttribute(-123123);
	}

	@Test
	public void testCreateRecords1() {
        RefBook refBook = refBookDao.get(1L);
        Date version = getDate(1, 1, 2012);

        List<Map<String, RefBookValue>> records = new ArrayList<Map<String, RefBookValue>>();
        int rowCount = 5;
        for (int i = 0; i < rowCount; i++) {
            Map<String, RefBookValue> record = refBook.createRecord();
            record.get(ATTRIBUTE_NAME).setValue("Название книги " + i);
            record.get(ATTRIBUTE_PAGECOUNT).setValue(100 + i);
            record.get(ATTRIBUTE_AUTHOR).setValue(6L);
            records.add(record);
        }
        refBookDao.createRecords(refBook.getId(), version, records);

        PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_NAME));
        Assert.assertEquals(rowCount, data.getRecords().size());
        for (int i = 0; i < rowCount; i++) {
            Map<String, RefBookValue> record = data.getRecords().get(i);
            Assert.assertEquals("Название книги " + i, record.get(ATTRIBUTE_NAME).getStringValue());
            Assert.assertEquals(100 + i, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().intValue());
            Assert.assertEquals(6, record.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
        }
    }

    @Test
    public void testCreateRecords2() {
        // Проверка округления
        RefBook refBook = refBookDao.get(1L);
        Date version = getDate(1, 1, 2012);
        List<Map<String, RefBookValue>> records = new ArrayList<Map<String, RefBookValue>>();

        Map<String, RefBookValue> record = refBook.createRecord();
        record.get(ATTRIBUTE_NAME).setValue("Название книги 1");
        record.get(ATTRIBUTE_PAGECOUNT).setValue(100);
        record.get(ATTRIBUTE_AUTHOR).setValue(6L);
        record.get(ATTRIBUTE_WEIGHT).setValue(0.9245);
        records.add(record);

        record = refBook.createRecord();
        record.get(ATTRIBUTE_NAME).setValue("Название книги 2");
        record.get(ATTRIBUTE_PAGECOUNT).setValue(500);
        record.get(ATTRIBUTE_AUTHOR).setValue(6L);
        record.get(ATTRIBUTE_WEIGHT).setValue(2.1344);
        records.add(record);

        refBookDao.createRecords(refBook.getId(), version, records);

        PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBook.getId(), version,
                new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_WEIGHT));
        Assert.assertEquals(2, data.getRecords().size());

        Assert.assertEquals(data.getRecords().get(0).get(ATTRIBUTE_WEIGHT).getNumberValue().doubleValue(), 0.925d, 0d);
        Assert.assertEquals(data.getRecords().get(1).get(ATTRIBUTE_WEIGHT).getNumberValue().doubleValue(), 2.134d, 0d);
    }

	/**
	 * Проверяем на пустом наборе данных
	 */
	@Test
	public void testCreateRecordsEmpty() {
		List<Map<String, RefBookValue>> records = new ArrayList<Map<String, RefBookValue>>();
		Date version = getDate(1, 3, 2013);
		refBookDao.createRecords(1L, version, records);
    }

	@Test
	public void testUpdateRecords1() {
		RefBook refBook = refBookDao.get(1L);
		Date version = getDate(1, 1, 2013);
		Date version2 = getDate(15, 1, 2013);

		// получаем данные для того, чтобы их изменить
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_NAME));
		Map<String, RefBookValue> record = data.getRecords().get(1);
		record.get(ATTRIBUTE_NAME).setValue("Вий. Туда и обратно");
		record.get(ATTRIBUTE_PAGECOUNT).setValue(123);
		record.get(ATTRIBUTE_AUTHOR).setValue(null);
		// сохраняем изменения
		List<Map<String, RefBookValue>> records = new ArrayList<Map<String, RefBookValue>>();
		records.add(record);
		refBookDao.updateRecords(refBook.getId(), version2, records);
		// проверяем изменения
		data = refBookDao.getRecords(refBook.getId(), version2, new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_NAME));
		record = data.getRecords().get(1);
		Assert.assertEquals("Вий. Туда и обратно", record.get(ATTRIBUTE_NAME).getStringValue());
		Assert.assertEquals(123, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().intValue());
		Assert.assertNull(record.get(ATTRIBUTE_AUTHOR).getReferenceValue());
		// проверяем, что предыдущая версия данных не была затронута
		data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_NAME));
		record = data.getRecords().get(1);
		Assert.assertEquals("Вий", record.get(ATTRIBUTE_NAME).getStringValue());
		Assert.assertEquals(425, record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().intValue());
		Assert.assertEquals(6, record.get(ATTRIBUTE_AUTHOR).getReferenceValue().intValue());
	}

    @Test
    public void testUpdateRecords2() {
        // Проверка обновления при совпадении версий
        Long refBookId = 1L;
        // Существующая дата
        Date version = getDate(1, 1, 2013);
        PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBookId, version, new PagingParams(), null, null);
        Map<String, RefBookValue> record = data.getRecords().get(1);

        record.get(RefBookDaoTest.ATTRIBUTE_NAME).setValue("Вий. Туда и обратно");
        record.get(ATTRIBUTE_PAGECOUNT).setValue(123);
        record.get(RefBookDaoTest.ATTRIBUTE_AUTHOR).setValue(7L);
        refBookDao.updateRecords(refBookId, version, Arrays.asList(record));

        data = refBookDao.getRecords(refBookId, version, new PagingParams(), null, null);
        record = data.getRecords().get(1);
        Assert.assertEquals(record.get(RefBookDaoTest.ATTRIBUTE_NAME).getStringValue(), "Вий. Туда и обратно");
        Assert.assertEquals(record.get(ATTRIBUTE_PAGECOUNT).getNumberValue().longValue(), 123L);
        Assert.assertEquals(record.get(RefBookDaoTest.ATTRIBUTE_AUTHOR).getReferenceValue(), Long.valueOf(7L));
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

        Map<String, RefBookValue> record = data.getRecords().get(0);
        record.get(ATTRIBUTE_WEIGHT).setValue(0.123456789d);
        refBookDao.updateRecords(refBookId, version, Arrays.asList(record));

        record = data.getRecords().get(1);
        record.get(ATTRIBUTE_WEIGHT).setValue(-345.9905);
        refBookDao.updateRecords(refBookId, version, Arrays.asList(record));

        data = refBookDao.getRecords(refBookId, version, new PagingParams(), null, null);

        Assert.assertEquals(data.getRecords().get(0).get(ATTRIBUTE_WEIGHT).getNumberValue().doubleValue(),
                0.123d, 0d);
        Assert.assertEquals(data.getRecords().get(1).get(ATTRIBUTE_WEIGHT).getNumberValue().doubleValue(),
                -345.991d, 0d);
    }

    @Test
    public void checkRecordUnique() {
       Assert.assertEquals(refBookDao.checkRecordUnique(1L, getDate(1, 1, 2013), 1L), Long.valueOf(1L));
       Assert.assertNull(refBookDao.checkRecordUnique(1L, getDate(2, 1, 2013), 1L));
    }

	@Test
	public void testDeleteRecords1() {
		RefBook refBook = refBookDao.get(1L);
		Date version = getDate(1, 2, 2013);
		// проверяем текущее количество записей
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_NAME));
		Assert.assertEquals(2, data.getRecords().size());

		Map<String, RefBookValue> record = data.getRecords().get(1);
		List<Long> recordIds = new ArrayList<Long>();
		recordIds.add(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
		refBookDao.deleteRecords(refBook.getId(), version, recordIds);
		// проверяем, что запись была удалена
		data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute(ATTRIBUTE_NAME));
		Assert.assertEquals(1, data.getRecords().size());
	}

    @Test
    public void testGetValue1() {
        // Cуществующие значения
        Assert.assertEquals(refBookDao.getValue(1L, 1L).getStringValue(), "Алиса в стране чудес");
        Assert.assertEquals(refBookDao.getValue(1L, 2L).getNumberValue().intValue(), 1113);
    }

    @Test(expected = DaoException.class)
    public void testGetValue2() {
        // Не существующее значение
        refBookDao.getValue(-1L, 2L);
    }

    @Test(expected = DuplicateKeyException.class)
    public void testDeleteAllRecords1() {
        // Удаление на дату совпадающую с какой-либо версией
        Date delDate = getDate(1, 1, 2013);
        Long rbId = 1L;

        refBookDao.deleteAllRecords(1L, delDate);

        PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(rbId, delDate, new PagingParams(), null,
                null);

        Assert.assertEquals(data.getRecords().size(), 0);
    }

    @Test
    public void testDeleteAllRecords2() {
        Date delDate = getDate(15, 1, 2013);
        Long rbId = 1L;
        // До удаления
        PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(rbId, delDate, new PagingParams(), null,
                null);
        Assert.assertTrue(data.getRecords().size() == 2);
        // Удаление
        refBookDao.deleteAllRecords(rbId, delDate);
        // После удаления
        data = refBookDao.getRecords(rbId, delDate, new PagingParams(), null, null);
        Assert.assertTrue(data.getRecords().size() == 0);
    }
}
