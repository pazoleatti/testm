package com.aplana.sbrf.taxaccounting.dao.script.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.script.dictionary.RefBookOkatoDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;


/**
 * @author Dmitriy Levykin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookOkatoDaoTest.xml"})
public class RefBookOkatoDaoTest {

    @Autowired
    private RefBookOkatoDao dao;

    @Autowired
    private RefBookDao rbDao;

    @Test
    public void clearParentId1() {
        Date version = getDate(1, 1, 2013);

        PagingResult<Map<String, RefBookValue>> result = rbDao.getRecords(3L, version,
                new PagingParams(), null, null);

        Assert.assertEquals(result.getRecords().size(), 3);

        boolean catcha = false;
        for (Map<String, RefBookValue> rec : result.getRecords()) {
           if (rec.get("PARENT_ID").getNumberValue() != null) {
               catcha = true;
           }
        }
        Assert.assertFalse(catcha);

        dao.clearParentId(version);

       result = rbDao.getRecords(3L, version, new PagingParams(), null, null);

        Assert.assertEquals(result.getRecords().size(), 3);

        for (Map<String, RefBookValue> rec : result.getRecords()) {
           Assert.assertNull(rec.get("PARENT_ID").getNumberValue());
        }
    }

    @Test
    public void updateParentIdTest1() {
        // Не задана версия
        Assert.assertEquals(dao.updateParentId(null), 0);
    }

    @Test
    public void updateParentIdTest2() {
        // Обновляются 2 записи
        Assert.assertEquals(dao.updateParentId(getDate(1, 1, 2013)), 2);
    }

    @Test
    public void updateParentIdTest3() {
        // Задана несуществующая версия
        Assert.assertEquals(dao.updateParentId(getDate(1, 1, 2010)), 0);
    }

    @Test
    public void updateValueNamesTest() {
        String okato1 = "57401365000", okato2 = "57401000000", okato3 = "123000000000",
                name1 = "Дзержинский_test", name2 = "Пермь_test", name3 = "test_test";
        Date version = getDate(1, 1, 2013);
        List<Map<String, RefBookValue>> recList = new LinkedList<Map<String, RefBookValue>>();
        HashMap<String, RefBookValue> map1 = new HashMap<String, RefBookValue>();
        map1.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name1));
        map1.put("OKATO", new RefBookValue(RefBookAttributeType.STRING, okato1));
        recList.add(map1);
        HashMap<String, RefBookValue> map2 = new HashMap<String, RefBookValue>();
        map2.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name2));
        map2.put("OKATO", new RefBookValue(RefBookAttributeType.STRING, okato2));
        recList.add(map2);
        HashMap<String, RefBookValue> map3 = new HashMap<String, RefBookValue>();
        map3.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name3));
        map3.put("OKATO", new RefBookValue(RefBookAttributeType.STRING, okato3));
        recList.add(map3);

        List<Map<String, RefBookValue>> updResult = dao.updateValueNames(version, recList);
        // Один код не должен быть найден
        Assert.assertEquals(updResult.size(), 1);
        Assert.assertEquals(updResult.get(0), map3);

        PagingResult<Map<String, RefBookValue>> result = rbDao.getRecords(3L, version,
                new PagingParams(), null, null);

        Assert.assertEquals(result.getRecords().size(), 3);

        for (Map<String, RefBookValue> rec : result.getRecords()) {
            if (rec.get("OKATO").getStringValue().equals(okato1)) {
                Assert.assertEquals(rec.get("NAME").getStringValue(), name1);
            } else if (rec.get("OKATO").getStringValue().equals(okato2)) {
                Assert.assertEquals(rec.get("NAME").getStringValue(), name2);
            } else {
                Assert.assertEquals(rec.get("OKATO").getStringValue(), "57000000000");
                Assert.assertEquals(rec.get("NAME").getStringValue(), "Пермский край");
            }
        }
    }

    private Date getDate(int day, int month, int year) {
        return new GregorianCalendar(year, month - 1, day, 15, 46, 57).getTime();
    }
}
