package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.RefBookDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "RefBookDaoTest.xml" })
@Transactional
public class RefBookDaoTest {

	@Autowired
	RefBookDao refBookDao;

	@Test
	public void testGet1() {
		RefBook refBook1 = refBookDao.get(1L);
		Assert.assertEquals(1, refBook1.getId().longValue());
		Assert.assertEquals(3, refBook1.getAttributes().size());
	}

	@Test
	public void testGet2() {
		RefBook refBook2 = refBookDao.get(2L);
		Assert.assertEquals(2, refBook2.getId().longValue());
		Assert.assertEquals(1, refBook2.getAttributes().size());
	}

	@Test
	public void testGetData1() throws Exception {
		RefBook refBook = refBookDao.get(1L);
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 1, 2013), null, null, refBook.getAttribute("name"));
		System.out.println(data);
		// проверяем кол-во строк
		Assert.assertEquals(2, data.getRecords().size());
		// проверяем типы значений
		for (int i = 0; i < 2; i++) {
			Map<String, RefBookValue> record = data.getRecords().get(0);
			Assert.assertEquals(RefBookAttributeType.NUMBER, record.get("id").getAttributeType());
			Assert.assertEquals(RefBookAttributeType.STRING, record.get("name").getAttributeType());
			Assert.assertEquals(RefBookAttributeType.NUMBER, record.get("pagecount").getAttributeType());
			Assert.assertEquals(RefBookAttributeType.REFERENCE, record.get("author").getAttributeType());
		}
		sort(data.getRecords());
		Map<String, RefBookValue> record = data.getRecords().get(0);
		Assert.assertEquals(1, record.get("id").getNumberValue().intValue());
		Assert.assertEquals("Алиса в стране чудес", record.get("name").getStringValue());
		Assert.assertEquals(1113, record.get("pagecount").getNumberValue().doubleValue(), 1e-5);
		Assert.assertEquals(5, record.get("author").getReferenceValue().intValue());

		Map<String, RefBookValue> record2 = data.getRecords().get(1);
		Assert.assertEquals(4, record2.get("id").getNumberValue().intValue());
		Assert.assertEquals("Вий", record2.get("name").getStringValue());
		Assert.assertEquals(425, record2.get("pagecount").getNumberValue().doubleValue(), 1e-5);
		Assert.assertEquals(6, record2.get("author").getReferenceValue().intValue());
	}

	@Test
	public void testGetData2() throws Exception {
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 2, 2013), null, null, null);
		System.out.println(data);
		// проверяем кол-во строк
		Assert.assertEquals(2, data.getRecords().size());
		sort(data.getRecords());
		Map<String, RefBookValue> record = data.getRecords().get(0);
		Assert.assertEquals(2, record.get("id").getNumberValue().intValue());
		Assert.assertEquals("Алиса в стране", record.get("name").getStringValue());
		Assert.assertEquals(1213, record.get("pagecount").getNumberValue().doubleValue(), 1e-5);
		Assert.assertEquals(7, record.get("author").getReferenceValue().intValue());
	}

	@Test
	public void testGetData3() throws Exception {
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(1L, getDate(1, 3, 2013), null, null, null);
		System.out.println(data);
		// проверяем кол-во строк
		Assert.assertEquals(1, data.getRecords().size());
		Map<String, RefBookValue> record2 = data.getRecords().get(0);
		Assert.assertEquals(4, record2.get("id").getNumberValue().intValue());
		Assert.assertEquals("Вий", record2.get("name").getStringValue());
		Assert.assertEquals(425, record2.get("pagecount").getNumberValue().doubleValue(), 1e-5);
		Assert.assertEquals(6L, record2.get("author").getReferenceValue().intValue());
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
				Long l1 = o1.get("id").getNumberValue().longValue();
				Long l2 = o2.get("id").getNumberValue().longValue();
				return l1.compareTo(l2);
			}
		});
	}

	@Test
	public void testGetAll() {
		List<RefBook> refBooks = refBookDao.getAll();
		Assert.assertEquals(2, refBooks.size());
	}

	@Test
	public void testGetRecordData(){
		Map<String, RefBookValue> record = refBookDao.getRecordData(1L, 4L);
		Assert.assertEquals(4, record.get("id").getNumberValue().intValue());
		Assert.assertEquals("Вий", record.get("name").getStringValue());
		Assert.assertEquals(425, record.get("pagecount").getNumberValue().doubleValue(), 1e-5);
		Assert.assertEquals(6, record.get("author").getReferenceValue().intValue());
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
			record.get("name").setValue("Название книги " + i);
			record.get("pagecount").setValue(100 + i);
			record.get("author").setValue(6L);
			records.add(record);
		}
		refBookDao.createRecords(refBook.getId(), version, records);

		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute("name"));
		Assert.assertEquals(rowCount, data.getRecords().size());
		for (int i = 0; i < rowCount; i++) {
			Map<String, RefBookValue> record = data.getRecords().get(i);
			Assert.assertEquals("Название книги " + i, record.get("name").getStringValue());
			Assert.assertEquals(100 + i, record.get("pagecount").getNumberValue().intValue());
			Assert.assertEquals(6, record.get("author").getReferenceValue().intValue());
		}
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
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute("name"));
		Map<String, RefBookValue> record = data.getRecords().get(1);
		record.get("name").setValue("Вий. Туда и обратно");
		record.get("pagecount").setValue(123);
		record.get("author").setValue(null);
		// сохраняем изменения
		List<Map<String, RefBookValue>> records = new ArrayList<Map<String, RefBookValue>>();
		records.add(record);
		refBookDao.updateRecords(refBook.getId(), version2, records);
		// проверяем изменения
		data = refBookDao.getRecords(refBook.getId(), version2, new PagingParams(), null, refBook.getAttribute("name"));
		record = data.getRecords().get(1);
		Assert.assertEquals("Вий. Туда и обратно", record.get("name").getStringValue());
		Assert.assertEquals(123, record.get("pagecount").getNumberValue().intValue());
		Assert.assertNull(record.get("author").getReferenceValue());
		// проверяем, что предыдущая версия данных не была затронута
		data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute("name"));
		record = data.getRecords().get(1);
		Assert.assertEquals("Вий", record.get("name").getStringValue());
		Assert.assertEquals(425, record.get("pagecount").getNumberValue().intValue());
		Assert.assertEquals(6, record.get("author").getReferenceValue().intValue());
	}

	@Test
	public void testDeleteRecords1() {
		RefBook refBook = refBookDao.get(1L);
		Date version = getDate(1, 2, 2013);
		// проверяем текущее количество записей
		PagingResult<Map<String, RefBookValue>> data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute("name"));
		Assert.assertEquals(2, data.getRecords().size());

		Map<String, RefBookValue> record = data.getRecords().get(1);
		List<Long> recordIds = new ArrayList<Long>();
		recordIds.add(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
		refBookDao.deleteRecords(refBook.getId(), version, recordIds);
		// проверяем, что запись была удалена
		data = refBookDao.getRecords(refBook.getId(), version, new PagingParams(), null, refBook.getAttribute("name"));
		Assert.assertEquals(1, data.getRecords().size());
	}

}
