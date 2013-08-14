package com.aplana.sbrf.taxaccounting.dao.script.refbook;

import com.aplana.sbrf.taxaccounting.dao.script.dictionary.RefBookOkatoDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.GregorianCalendar;


/**
 * @author Dmitriy Levykin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookOkatoDaoTest.xml"})
public class RefBookOkatoDaoTest {

    @Autowired
    private RefBookOkatoDao dao;

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
