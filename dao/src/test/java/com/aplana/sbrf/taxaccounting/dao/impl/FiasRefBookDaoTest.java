package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.FiasRefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Table;
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

    private static final int ADDR_OBJECT_ROW_CNT = 21;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private FiasRefBookDao fiasRefBookDao;

    @Autowired
    RefBookDao refBookDao;


    @Test
    public void testAddressObject() {

        //В скрипте создаем 21 записей
        Assert.assertEquals(ADDR_OBJECT_ROW_CNT, refBookDao.getRecordsCount(Id.FIAS_ADDR_OBJECT.getId(), Table.FIAS_ADDR_OBJECT.getTable(), null));

        //создаем 3 записи через DAO
        fiasRefBookDao.insertRecordsBatch(Table.FIAS_ADDR_OBJECT.getTable(), createAddrObjectRecords());

        Assert.assertEquals(ADDR_OBJECT_ROW_CNT + 3, refBookDao.getRecordsCount(Id.FIAS_ADDR_OBJECT.getId(), Table.FIAS_ADDR_OBJECT.getTable(), null));

        //Удаляем все записи
        fiasRefBookDao.clearAll();

        Assert.assertEquals(0, refBookDao.getRecordsCount(Id.FIAS_ADDR_OBJECT.getId(), Table.FIAS_ADDR_OBJECT.getTable(), null));
    }

    @Test
    public void findRegionByCodeTest() {
        AddressObject result = fiasRefBookDao.findRegionByCode("01");
        Assert.assertEquals(Long.valueOf(1), result.getId());
    }

    @Test
    public void findAddressUtils() {
        Assert.assertEquals("#foo#bar#baz", FiasRefBookDaoImpl.createPath("foo", "bar", "baz"));
        Assert.assertEquals("#foo#bar", FiasRefBookDaoImpl.createPath("foo", null, "bar", null));
        Assert.assertEquals("#foo", FiasRefBookDaoImpl.createPath("foo"));
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

    private static List<Map<String, Object>> createAddrObjectRecords() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        result.add(createAddrObjectRow(new Object[]{"7", 0, 0, "000", "Ветеранов 3-я", 1, "001", "Ветеранов 3-я", 0, "0000", "385000", 1, 2, "0026", "01", "0", "0000", 22, 22, "ул", "000", "000", "000"}));
        result.add(createAddrObjectRow(new Object[]{"7", 0, 0, "000", "Береговая", 1, "001", "Береговая", 0, "0000", "385012", 1, 2, "0017", "01", "0", "0000", 23, 23, "ул", "000", "000", "000"}));
        result.add(createAddrObjectRow(new Object[]{"7", 0, 0, "000", "Братьев Соловьевых", 1, "001", "Братьев Соловьевых", 0, "0000", "385000", 1, 2, "0019", "01", "0", "0000", 24, 24, "пер", "000", "000", "000"}));
        return result;
    }


    private static final int AOLEVEL = 0, CURRSTATUS = 1, CENTSTATUS = 2, SEXTCODE = 3, FORMALNAME = 4, LIVESTATUS = 5,
            CITYCODE = 6, OFFNAME = 7, DIVTYPE = 8, PLANCODE = 9, POSTALCODE = 10, OPERSTATUS = 11, PARENTGUID = 12,
            STREETCODE = 13, REGIONCODE = 14, AUTOCODE = 15, EXTRCODE = 16, ID = 17, AOID = 18, SHORTNAME = 19,
            CTARCODE = 20, AREACODE = 21, PLACECODE = 22;


    private static Map<String, Object> createAddrObjectRow(Object[] values) {

        Map<String, Object> recordsMap = new HashMap<String, Object>();
        recordsMap.put("ID", values[ID]);
        recordsMap.put("AOID", values[AOID]);
        recordsMap.put("PARENTGUID", values[PARENTGUID]);
        recordsMap.put("FORMALNAME", values[FORMALNAME]);
        recordsMap.put("SHORTNAME", values[SHORTNAME]);
        recordsMap.put("REGIONCODE", values[REGIONCODE]);
        recordsMap.put("AUTOCODE", values[AUTOCODE]);
        recordsMap.put("AREACODE", values[AREACODE]);
        recordsMap.put("CITYCODE", values[CITYCODE]);
        recordsMap.put("CTARCODE", values[CTARCODE]);
        recordsMap.put("PLACECODE", values[PLACECODE]);
        recordsMap.put("PLANCODE", values[PLANCODE]);
        recordsMap.put("STREETCODE", values[STREETCODE]);
        recordsMap.put("EXTRCODE", values[EXTRCODE]);
        recordsMap.put("SEXTCODE", values[SEXTCODE]);
        recordsMap.put("LIVESTATUS", values[LIVESTATUS]);
        recordsMap.put("CENTSTATUS", values[CENTSTATUS]);
        recordsMap.put("OPERSTATUS", values[OPERSTATUS]);
        recordsMap.put("CURRSTATUS", values[CURRSTATUS]);
        //Поле тип адресации. Хотя поле обязательное в выгрузке его нет, ставим значение 0 - не определено
        recordsMap.put("DIVTYPE", values[DIVTYPE]);
        recordsMap.put("OFFNAME", values[OFFNAME]);
        recordsMap.put("AOLEVEL", values[AOLEVEL]);
        recordsMap.put("POSTALCODE", values[POSTALCODE]);
        return recordsMap;
    }

}
