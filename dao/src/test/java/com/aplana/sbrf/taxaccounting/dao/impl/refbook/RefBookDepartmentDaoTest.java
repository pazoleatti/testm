package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookDepartmentDaoTest.xml"})
@Transactional
public class RefBookDepartmentDaoTest {
    private final static int DEPARTMENTS_COUNT = 2;
    private final static int DEPARTMENTS_TOTAL_RECORDS = DEPARTMENTS_COUNT;

    @Autowired
    RefBookDepartmentDao refBookDepartmentDao;

    @Test
    public void getRecords() {
        // Получим записи из бд
        RefBookValue departmentBankid = new RefBookValue(RefBookAttributeType.NUMBER, 1L);
        RefBookValue departmentTB1Name = new RefBookValue(RefBookAttributeType.STRING, "Териториальный Банк №1");
        PagingResult<Map<String, RefBookValue>> data = refBookDepartmentDao.getRecords(new PagingParams(), null, null);
        assertTrue(data.getTotalCount() == DEPARTMENTS_TOTAL_RECORDS);
        assertTrue(data.size() == DEPARTMENTS_COUNT);
        assertTrue(data.get(0).get(RefBook.RECORD_ID_ALIAS).equals(departmentBankid));
        assertTrue(data.get(1).get("name").equals(departmentTB1Name));

        // Получим пустой результат (уйдем за пределы пагинации)
        data = refBookDepartmentDao.getRecords(new PagingParams(999999, 10), null, null);
        assertTrue(data.getTotalCount() == DEPARTMENTS_TOTAL_RECORDS);
        assertTrue(data.size() == 0);

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
        RefBookValue expectedValue = new RefBookValue(RefBookAttributeType.STRING, "Териториальный Банк №1");
        assertTrue(record.get("name").equals(expectedValue));
    }

}
