package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookUserDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Тесты для
 * @author auldanov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookUserDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookUserDaoTest {

    private final static int USER_TOTAL_RECORDS = 4;

    @Autowired
    RefBookUserDao refBookUserDao;

    @Test
    public void getRecords(){
        // получим все записи с бд
        PagingResult<Map<String, RefBookValue>> data = refBookUserDao.getRecords(new PagingParams(), null, null);
        assertTrue(data.getTotalCount() == USER_TOTAL_RECORDS);
        assertTrue(data.size() == USER_TOTAL_RECORDS);

        // Получим пустой результат (уйдем за пределы пагинации)
        data = refBookUserDao.getRecords(new PagingParams(999999, 10), null, null);
        assertTrue(data.getTotalCount() == USER_TOTAL_RECORDS);
        assertTrue(data.isEmpty());

        // Проверка фильтрации
        data = refBookUserDao.getRecords(new PagingParams(), "NAME like '%ТБ%'", null);
        assertTrue(data.size() == 2);
        assertTrue(data.get(0).get("NAME").equals(new RefBookValue(RefBookAttributeType.STRING, "Контролёр ТБ1")));
        assertTrue(data.get(1).get("NAME").equals(new RefBookValue(RefBookAttributeType.STRING, "Контролёр ТБ2")));

        // Получение данных без пагинации
        data = refBookUserDao.getRecords(null, "NAME like '%ТБ%'", null);
        assertTrue(data.size() == 2);
        assertTrue(data.get(0).get("NAME").equals(new RefBookValue(RefBookAttributeType.STRING, "Контролёр ТБ1")));
    }

    @Test
    public void getRecordData(){
        Map<String, RefBookValue> record = refBookUserDao.getRecordData(2L);
        RefBookValue expectedValue = new RefBookValue(RefBookAttributeType.STRING, "Контролёр ТБ2");
        assertTrue(record.get("NAME").equals(expectedValue));
    }
}
