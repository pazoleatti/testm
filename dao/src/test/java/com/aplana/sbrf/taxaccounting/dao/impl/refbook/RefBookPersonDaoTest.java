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

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.core.Is.is;

/**
 * @author Andrey Drunk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookPersonDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookPersonDaoTest {

    public static final long RUS_COUNTRY_ID = 262254399L;

    @Autowired
    RefBookDao refBookDao;

    @Autowired
    private RefBookPersonDaoImpl refBookPersonDao;

    @Autowired
    private RefBookSimpleDao dao;

    private static Date actualDate;

    @BeforeClass
    public static void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, 6, 1);
        actualDate = calendar.getTime();
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
}
