package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.FiasRefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FiasRefBookDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FiasRefBookDaoTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private FiasRefBookDao fiasRefBookDao;

    @Autowired
    RefBookDao refBookDao;

    @Test
    public void testOperstat() {

        //В скрипте создаем 17 записей
        Assert.assertEquals(17, refBookDao.getRecordsCount(FiasRefBookDaoImpl.OPERSTAT_ID, FiasRefBookDaoImpl.OPERSTAT_TABLE_NAME, null));

        fiasRefBookDao.insertRecordsBatch(FiasRefBookDaoImpl.OPERSTAT_TABLE_NAME, createOperstatRecords());

        Assert.assertEquals(20, refBookDao.getRecordsCount(FiasRefBookDaoImpl.OPERSTAT_ID, FiasRefBookDaoImpl.OPERSTAT_TABLE_NAME, null));

        //Удаление
        fiasRefBookDao.clearAll();

        Assert.assertEquals(0, refBookDao.getRecordsCount(FiasRefBookDaoImpl.OPERSTAT_ID, FiasRefBookDaoImpl.OPERSTAT_TABLE_NAME, null));
        Assert.assertEquals(0, refBookDao.getRecordsCount(FiasRefBookDaoImpl.SOCRBASE_ID, FiasRefBookDaoImpl.SOCRBASE_TABLE_NAME, null));
        Assert.assertEquals(0, refBookDao.getRecordsCount(FiasRefBookDaoImpl.ADDR_OBJECT_ID, FiasRefBookDaoImpl.ADDR_OBJECT_TABLE_NAME, null));
        Assert.assertEquals(0, refBookDao.getRecordsCount(FiasRefBookDaoImpl.HOUSE_ID, FiasRefBookDaoImpl.HOUSE_TABLE_NAME, null));
        Assert.assertEquals(0, refBookDao.getRecordsCount(FiasRefBookDaoImpl.HOUSEINT_ID, FiasRefBookDaoImpl.HOUSEINT_TABLE_NAME, null));
        Assert.assertEquals(0, refBookDao.getRecordsCount(FiasRefBookDaoImpl.ROOM_ID, FiasRefBookDaoImpl.ROOM_TABLE_NAME, null));
    }

    private static List<Map<String, Object>> createOperstatRecords(){
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        result.add(createOperstatRow(111L, "Не определено"));
        result.add(createOperstatRow(222L, "Инициация"));
        result.add(createOperstatRow(333L, "Добавление"));
        return result;
    }

    public static Map<String, Object> createOperstatRow(long id, String name){
        Map<String, Object> recordsMap = new HashMap<String, Object>();
        recordsMap.put("ID", id);
        recordsMap.put("NAME", name);
        return recordsMap;
    }

}
