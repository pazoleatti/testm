package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.result.ReportPeriodResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ReportPeriodDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReportPeriodDaoTest {

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    private TaxPeriod taxPeriod;

    @Test(expected = DaoException.class)
    public void getNotExistentTest() {
        reportPeriodDao.fetchOne(-1);
    }

    @Before
    public void init() {
        taxPeriod = new TaxPeriod();
        taxPeriod.setId(31);
        taxPeriod.setTaxType(TaxType.NDFL);
        taxPeriod.setYear(2018);
    }

    @Test
    public void test_fetchAll() {
        List<ReportPeriod> allPeriods = reportPeriodDao.findAll();
        assertThat(allPeriods).hasSize(3);
    }

    @Test
    // TODO: Тест ничего не делает
    public void getCorrectPeriods() {
        PagingParams pagingParams = new PagingParams();
        pagingParams.setProperty("id");
        pagingParams.setDirection("ASC");
        reportPeriodDao.getCorrectPeriods(1);
    }

    @Test
    public void listByTaxPeriodSuccessfulTest() {
        ReportPeriod newReportPeriod = new ReportPeriod();
        newReportPeriod.setName("MyTestName1");
        newReportPeriod.setTaxPeriod(taxPeriod);
        newReportPeriod.setDictTaxPeriodId(21);
        newReportPeriod.setStartDate(new Date());
        newReportPeriod.setEndDate(new Date());
        newReportPeriod.setCalendarStartDate(new GregorianCalendar(2014, Calendar.JANUARY, 1).getTime());
        reportPeriodDao.create(newReportPeriod);

        newReportPeriod = new ReportPeriod();
        newReportPeriod.setName("MyTestName2");
        newReportPeriod.setTaxPeriod(taxPeriod);
        newReportPeriod.setDictTaxPeriodId(22);
        newReportPeriod.setStartDate(new Date());
        newReportPeriod.setEndDate(new Date());
        newReportPeriod.setCalendarStartDate(new GregorianCalendar(2014, Calendar.JANUARY, 1).getTime());
        reportPeriodDao.create(newReportPeriod);

        List<ReportPeriod> reportPeriodList = reportPeriodDao.fetchAllByTaxPeriod(taxPeriod.getId());
        assertEquals(2, reportPeriodList.size());

        reportPeriodList = reportPeriodDao.fetchAllByTaxPeriod(-1);
        assertEquals(0, reportPeriodList.size());
    }

    @Test
    public void saveAndGetSuccessTest() {
        ReportPeriod newReportPeriod = new ReportPeriod();
        newReportPeriod.setName("MyTestName");
        newReportPeriod.setTaxPeriod(taxPeriod);
        newReportPeriod.setDictTaxPeriodId(21);
        newReportPeriod.setStartDate(new Date());
        newReportPeriod.setEndDate(new Date());
        newReportPeriod.setCalendarStartDate(new GregorianCalendar(2014, Calendar.JANUARY, 1).getTime());

        int newReportPeriodId = reportPeriodDao.create(newReportPeriod);
        ReportPeriod reportPeriod = reportPeriodDao.fetchOne(newReportPeriodId);

        assertEquals("MyTestName", reportPeriod.getName());
        assertEquals(taxPeriod.getId(), reportPeriod.getTaxPeriod().getId());
        assertEquals(taxPeriod.getId(), reportPeriod.getTaxPeriod().getId());
        assertEquals(21, reportPeriod.getDictTaxPeriodId());
    }

    @Test
    public void getReportPeriodByTaxPeriodAndDictTest1() {
        ReportPeriod reportPeriod1 = reportPeriodDao.fetchOneByTaxPeriodAndDict(1, 21);
        ReportPeriod reportPeriod2 = reportPeriodDao.fetchOneByTaxPeriodAndDict(1, 22);
        assertEquals(reportPeriod1.getId(), Integer.valueOf(1));
        assertEquals(reportPeriod2.getId(), Integer.valueOf(2));
    }

    @Test
    public void getReportPeriodByTaxPeriodAndDictTest2() {
        assertNull(reportPeriodDao.fetchOneByTaxPeriodAndDict(-1, -1));
    }

    private List<Integer> getReportPeriodIds(List<ReportPeriod> reportPeriodList) {
        List<Integer> retVal = new LinkedList<>();
        for (ReportPeriod reportPeriod : reportPeriodList) {
            retVal.add(reportPeriod.getId());
        }
        return retVal;
    }

    @Test
    public void getPeriodsByTaxTypeAndDepartmentsTest() {
        List<ReportPeriod> reportPeriods;
        reportPeriods = reportPeriodDao.fetchAllByDepartments(asList(1, 2, 3));
        assertEquals(3, reportPeriods.size());
        assertTrue(getReportPeriodIds(reportPeriods).containsAll(asList(1, 2, 3)));
    }

    @Test
    public void getAllActivePeriods() {
        List<ReportPeriod> reportPeriods;
        reportPeriods = reportPeriodDao.findAllActive(asList(1, 2, 3));
        assertEquals(3, reportPeriods.size());
        assertTrue(getReportPeriodIds(reportPeriods).containsAll(asList(1, 2, 3)));
    }

    private ReportPeriod getReportPeriod() {
        ReportPeriod newReportPeriod = new ReportPeriod();
        newReportPeriod.setName("MyTestName");
        newReportPeriod.setTaxPeriod(taxPeriod);
        newReportPeriod.setDictTaxPeriodId(21);
        newReportPeriod.setStartDate(new Date());
        newReportPeriod.setEndDate(new Date());
        return newReportPeriod;
    }

    @Test(expected = DaoException.class)
    public void calendarStartDateTest1() {
        ReportPeriod newReportPeriod = getReportPeriod();
        newReportPeriod.setCalendarStartDate(new GregorianCalendar(2014, Calendar.JANUARY, 25).getTime());
        int newReportPeriodId = reportPeriodDao.create(newReportPeriod);
        reportPeriodDao.fetchOne(newReportPeriodId);
    }

    @Test(expected = DaoException.class)
    public void calendarStartDateTest2() {
        ReportPeriod newReportPeriod = getReportPeriod();
        newReportPeriod.setCalendarStartDate(new GregorianCalendar(2014, Calendar.FEBRUARY, 1).getTime());
        int newReportPeriodId = reportPeriodDao.create(newReportPeriod);
        reportPeriodDao.fetchOne(newReportPeriodId);
    }

    @Test(expected = DaoException.class)
    public void calendarStartDateTest3() {
        ReportPeriod newReportPeriod = getReportPeriod();
        newReportPeriod.setCalendarStartDate(new GregorianCalendar(2014, Calendar.MARCH, 10).getTime());
        int newReportPeriodId = reportPeriodDao.create(newReportPeriod);
        reportPeriodDao.fetchOne(newReportPeriodId);
    }

    @Test
    public void calendarStartDateTest4() {
        ReportPeriod newReportPeriod = getReportPeriod();
        Date date = new GregorianCalendar(2014, Calendar.APRIL, 1).getTime();
        newReportPeriod.setCalendarStartDate(date);
        int newReportPeriodId = reportPeriodDao.create(newReportPeriod);
        ReportPeriod reportPeriod = reportPeriodDao.fetchOne(newReportPeriodId);
        assertEquals(date, reportPeriod.getCalendarStartDate());
        assertEquals(2, reportPeriod.getOrder());
    }

    @Test
    public void getByTaxTypedCodeYearTest() {
        ReportPeriod reportPeriod1 = reportPeriodDao.getByTaxTypedCodeYear("21", 2013);
        ReportPeriod reportPeriod2 = reportPeriodDao.getByTaxTypedCodeYear("99", 2015);
        assertNull(reportPeriod1);
        assertNotNull(reportPeriod2);
        assertEquals(3, reportPeriod2.getId().intValue());
    }

    @Test
    public void getReportPeriodTypeById() {
        assertEquals(reportPeriodDao.getReportPeriodTypeById(21L).getCode(), "99");
    }

    @Test
    public void test_FetchActiveByDepartment() {
        List<ReportPeriodResult> result = reportPeriodDao.fetchActiveByDepartment(1);
        assertThat(result.size(), is(3));
    }
}