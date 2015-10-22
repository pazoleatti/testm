package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookDepartmentDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookDepartmentDaoTest {
    private final static int DEPARTMENTS_COUNT = 2;
    private final static int DEPARTMENTS_TOTAL_RECORDS = DEPARTMENTS_COUNT;

    @Autowired
    RefBookDepartmentDao refBookDepartmentDao;
    @Autowired
    RefBookDao refBookDao;
    @Autowired
    DepartmentDao departmentDao;

    @Test
    public void getRecords() {
        // Получим записи из бд
        RefBookValue departmentBankid = new RefBookValue(RefBookAttributeType.NUMBER, 1L);
        RefBookValue departmentTB1Name = new RefBookValue(RefBookAttributeType.STRING, "Территориальный Банк №1");
        PagingResult<Map<String, RefBookValue>> data = refBookDepartmentDao.getRecords(new PagingParams(), null, null);
        assertTrue(data.getTotalCount() == DEPARTMENTS_TOTAL_RECORDS);
        assertTrue(data.size() == DEPARTMENTS_COUNT);
        assertTrue(data.get(0).get(RefBook.RECORD_ID_ALIAS).equals(departmentBankid));
        assertTrue(data.get(1).get("name").equals(departmentTB1Name));

        // Получим пустой результат (уйдем за пределы пагинации)
        data = refBookDepartmentDao.getRecords(new PagingParams(999999, 10), null, null);
        assertTrue(data.getTotalCount() == DEPARTMENTS_TOTAL_RECORDS);
        assertTrue(data.isEmpty());

        // Получим записи из бд с сортировкой (метод сейчаc падает из за отсутвия сортировки
//        RefBookAttribute sortAttribute = new RefBookAttribute();
//        sortAttribute.setAlias("sbrf_code");
//        data = refBookDepartmentDao.getRecords(DEPARTMENT_REF_BOOK_ID, new PagingParams(), sortAttribute);
//        assertTrue(data.getTotalCount() == DEPARTMENTS_TOTAL_RECORDS);
//        assertTrue(data.size() == DEPARTMENTS_COUNT);
//        assertTrue(data.get(1).get(RefBook.RECORD_ID_ALIAS).equals(departmentBankid));
//        assertTrue(data.get(0).get("name").equals(departmentTB1Name));

        // Проверка фильтрации
        data = refBookDepartmentDao.getRecords(new PagingParams(), "sbrf_code like '%003%'", null);
        assertTrue(data.size() == 1);
        assertTrue(data.get(0).get("name").equals(departmentTB1Name));

        // Получение данных без пагинации
        data = refBookDepartmentDao.getRecords(null, "sbrf_code like '%003%'", null);
        assertTrue(data.size() == 1);
        assertTrue(data.get(0).get("name").equals(departmentTB1Name));
    }

    @Test
    public void getRecordData() {
        Map<String, RefBookValue> record = refBookDepartmentDao.getRecordData(2L);
        RefBookValue expectedValue = new RefBookValue(RefBookAttributeType.STRING, "Территориальный Банк №1");
        assertTrue(record.get("name").equals(expectedValue));
    }

    @Test
    public void testGetMatchedRecordsByUniqueAttributes() {
        RefBook refBook = refBookDao.get(30L);
        PagingResult<Map<String, RefBookValue>> allValues = new PagingResult<Map<String, RefBookValue>>();
        allValues.add(new HashMap<String, RefBookValue>(){{
            put("code", new RefBookValue(RefBookAttributeType.NUMBER, 1));
            put("name", new RefBookValue(RefBookAttributeType.STRING, "Главный Банк"));
        }});
        List<RefBookRecord> records = new ArrayList<RefBookRecord>();
        for (Map<String, RefBookValue> values : allValues) {
            RefBookRecord record = new RefBookRecord();
            record.setValues(values);
            record.setRecordId(null);
            records.add(record);
        }
        List<Pair<String,String>> matches =
                refBookDepartmentDao.getMatchedRecordsByUniqueAttributes(100l, refBook.getAttributes(), records);
        assertEquals(2, matches.size());
        List<Pair<String,String>> matchesNull =
                refBookDepartmentDao.getMatchedRecordsByUniqueAttributes(null, refBook.getAttributes(), records);
        assertEquals(2, matchesNull.size());
    }

    @Test
    public void testUpdate() {
        Map<String, RefBookValue> record = refBookDepartmentDao.getRecordData(2L);
        refBookDepartmentDao.update(30, record, refBookDao.getAttributes(30l));
    }

    @Test
    public void testCreate() {

		RefBook refBook = refBookDao.get(RefBookDepartmentDao.REF_BOOK_ID);
		Map<String, RefBookValue> record = refBook.createRecord();
		record.get("name").setValue("dsfs");
		record.get("sbrf_code").setValue("99_0000_00");
		record.get("type").setValue(Long.valueOf(DepartmentType.MANAGEMENT.getCode()));
		record.get("code").setValue(101);
        assertEquals(1000, refBookDepartmentDao.create(record, refBookDao.getAttributes(30l)));
    }

    @Test(expected = DaoException.class)
    public void testRemove() {
        refBookDepartmentDao.remove(1);
        departmentDao.getDepartment(1);
    }

}
