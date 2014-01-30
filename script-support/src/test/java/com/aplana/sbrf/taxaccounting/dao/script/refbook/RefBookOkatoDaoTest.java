package com.aplana.sbrf.taxaccounting.dao.script.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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

        Assert.assertEquals(result.size(), 3);

        boolean catcha = false;
        for (Map<String, RefBookValue> rec : result) {
           if (rec.get("PARENT_ID").getNumberValue() != null) {
               catcha = true;
           }
        }
        Assert.assertFalse(catcha);

        dao.clearParentId(version);

       result = rbDao.getRecords(3L, version, new PagingParams(), null, null);

        Assert.assertEquals(result.size(), 3);

        for (Map<String, RefBookValue> rec : result) {
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

    private Date getDate(int day, int month, int year) {
        return new GregorianCalendar(year, month - 1, day, 15, 46, 57).getTime();
    }
}
