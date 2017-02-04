package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.FiasRefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;
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
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Table;



import java.util.*;

/**
 * @author Andrey Drunk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FiasRefBookDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FiasRefBookDaoTest {

    private static final int OPERSTAT_ROW_CNT = 17;
    private static final int ADDR_OBJECT_ROW_CNT = 24;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private FiasRefBookDao fiasRefBookDao;

    @Autowired
    RefBookDao refBookDao;

    @Test
    public void testOperstat() {

        //В скрипте создаем 17 записей
        Assert.assertEquals(OPERSTAT_ROW_CNT, refBookDao.getRecordsCount(Id.FIAS_OPERSTAT.getId(), Table.FIAS_OPERSTAT.getTable(), null));

        fiasRefBookDao.insertRecordsBatch(Table.FIAS_OPERSTAT.getTable(), createOperstatRecords());

        Assert.assertEquals(20, refBookDao.getRecordsCount(Id.FIAS_OPERSTAT.getId(), Table.FIAS_OPERSTAT.getTable(), null));

        //Удаление
        fiasRefBookDao.clearAll();

        Assert.assertEquals(0, refBookDao.getRecordsCount(Id.FIAS_OPERSTAT.getId(), Table.FIAS_OPERSTAT.getTable(), null));
        Assert.assertEquals(0, refBookDao.getRecordsCount(Id.FIAS_SOCRBASE.getId(), Table.FIAS_SOCRBASE.getTable(), null));
        Assert.assertEquals(0, refBookDao.getRecordsCount(Id.FIAS_ADDR_OBJECT.getId(), Table.FIAS_ADDR_OBJECT.getTable(), null));
        Assert.assertEquals(0, refBookDao.getRecordsCount(Id.FIAS_HOUSE.getId(), Table.FIAS_HOUSE.getTable(), null));
        Assert.assertEquals(0, refBookDao.getRecordsCount(Id.FIAS_HOUSEINT.getId(), Table.FIAS_HOUSEINT.getTable(), null));
        Assert.assertEquals(0, refBookDao.getRecordsCount(Id.FIAS_ROOM.getId(), Table.FIAS_ROOM.getTable(), null));
    }

    @Test
    public void testAddressObject() {
        Assert.assertEquals(ADDR_OBJECT_ROW_CNT, refBookDao.getRecordsCount(Id.FIAS_ADDR_OBJECT.getId(), Table.FIAS_ADDR_OBJECT.getTable(), null));
    }

    @Test
    public void findRegionByCodeTest(){
        AddressObject result = fiasRefBookDao.findRegionByCode("01");
        Assert.assertEquals(Long.valueOf(1), result.getId());
    }

    @Test
    public void findAddressUtils(){
        Assert.assertEquals("\\\\foo\\\\bar\\\\baz", FiasRefBookDaoImpl.createPath("foo", "bar", "baz"));
        Assert.assertEquals("\\\\foo\\\\bar", FiasRefBookDaoImpl.createPath("foo", null, "bar", null));
        Assert.assertEquals("\\\\foo", FiasRefBookDaoImpl.createPath("foo"));
        Assert.assertEquals(null, FiasRefBookDaoImpl.createPath(""));
        Assert.assertEquals(null, FiasRefBookDaoImpl.createPath(null));
        Assert.assertEquals(null, FiasRefBookDaoImpl.createPath());

        Assert.assertEquals("baz", FiasRefBookDaoImpl.getLeaf("foo", "bar", "baz"));
        Assert.assertEquals("bar", FiasRefBookDaoImpl.getLeaf("foo", null, "bar", null));
        Assert.assertEquals("foo", FiasRefBookDaoImpl.getLeaf("foo"));
        Assert.assertEquals(null, FiasRefBookDaoImpl.getLeaf(""));
        Assert.assertEquals(null, FiasRefBookDaoImpl.getLeaf(null));
        Assert.assertEquals(null, FiasRefBookDaoImpl.getLeaf());
    }

    private static List<Map<String, Object>> createOperstatRecords(){
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        result.add(createOperstatRow(111L, "Не определено"));
        result.add(createOperstatRow(222L, "Инициация"));
        result.add(createOperstatRow(333L, "Добавление"));
        return result;
    }

    private static Map<String, Object> createOperstatRow(long id, String name){
        Map<String, Object> recordsMap = new HashMap<String, Object>();
        recordsMap.put("ID", id);
        recordsMap.put("NAME", name);
        return recordsMap;
    }

}
