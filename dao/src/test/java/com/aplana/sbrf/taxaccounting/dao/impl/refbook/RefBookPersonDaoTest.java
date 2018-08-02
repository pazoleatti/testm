package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.core.Is.is;

/**
 * @author Andrey Drunk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookPersonDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RefBookPersonDaoTest {

    public static final long RUS_COUNTRY_ID = 262254399L;

    @Autowired
    RefBookDao refBookDao;

    @Autowired
    private RefBookPersonDaoImpl refBookPersonDao;

    @Autowired
    private RefBookSimpleDao dao;

    private static Date actualDate;

    private static Method createHintMethod;

    @BeforeClass
    public static void setUp() throws NoSuchMethodException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, 6, 1);
        actualDate = calendar.getTime();
        Class <? extends RefBookPersonDaoImpl> clazz = RefBookPersonDaoImpl.class;
        createHintMethod = clazz.getDeclaredMethod("createHint", String.class);
        createHintMethod.setAccessible(true);
    }

    @Test
    public void testGetAllPersons() {
        PagingResult<RefBookPerson> result = refBookPersonDao.getPersons(actualDate, null, null, null);
        Assert.assertThat(result.size(), is(4));
    }

    @Test
    public void testFetchingOriginalWhereDuplicateVersionIsLater() {
        PagingResult<RefBookPerson> result = refBookPersonDao.getPersons(actualDate, null, "last_name like '%Сульжик%'", null);
        Assert.assertThat("Perhaps it is necessary to pay attention to the attributes of the version and status in the query", result.size(), is(1));
    }

    @Test
    public void test_createHint_empty() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createHintMethod.invoke(refBookPersonDao, "");
        Assert.assertThat(result, is("/*+ FIRST_ROWS */"));
    }

    @Test
    public void test_createHint_filled() throws InvocationTargetException, IllegalAccessException {
        String result = (String) createHintMethod.invoke(refBookPersonDao,"filter");
        Assert.assertThat(result, is("/*+ PARALLEL(16) */"));
    }

    @Test
    public void test_getPersons() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(2017, 0, 1);
        Date version = calendar.getTime();
        String filter = "middle_name like '%Сергеевич%'";
        PagingResult<RefBookPerson> result = refBookPersonDao.getPersons(version, null, filter, null);
        Assert.assertThat(result.size(), is(2));
    }

}
