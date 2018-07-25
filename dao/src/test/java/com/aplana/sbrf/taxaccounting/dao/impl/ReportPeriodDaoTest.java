package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Assert;
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
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ReportPeriodDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReportPeriodDaoTest {

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Autowired
	private TaxPeriodDao taxPeriodDao;

	private TaxPeriod taxPeriod;

	@Test(expected = DaoException.class)
	public void getNotExistentTest() {
		reportPeriodDao.fetchOne(-1);
	}
	
	@Before
	public void init(){
		taxPeriod = new TaxPeriod();
		taxPeriod.setTaxType(TaxType.NDFL);
		taxPeriod.setYear(Calendar.getInstance().get(Calendar.YEAR));
		taxPeriod.setId(taxPeriodDao.create(taxPeriod));
	}

    @Test
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
		newReportPeriod.setCalendarStartDate(new GregorianCalendar(2014,Calendar.JANUARY,1).getTime());
		reportPeriodDao.create(newReportPeriod);
		
		newReportPeriod = new ReportPeriod();
		newReportPeriod.setName("MyTestName2");
		newReportPeriod.setTaxPeriod(taxPeriod);
		newReportPeriod.setDictTaxPeriodId(22);
		newReportPeriod.setStartDate(new Date());
		newReportPeriod.setEndDate(new Date());
		newReportPeriod.setCalendarStartDate(new GregorianCalendar(2014,Calendar.JANUARY,1).getTime());
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
        Assert.assertNull(reportPeriodDao.fetchOneByTaxPeriodAndDict(-1, -1));
    }

    private List<Integer> getReportPeriodIds(List<ReportPeriod> reportPeriodList) {
        List<Integer> retVal = new LinkedList<Integer>();
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
        Assert.assertTrue(getReportPeriodIds(reportPeriods).containsAll(asList(1, 2, 3)));
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
        Assert.assertNull(reportPeriod1);
        Assert.assertNotNull(reportPeriod2);
        assertEquals(3, reportPeriod2.getId().intValue());
    }

    @Test
    public void getReportPeriodsByDateTest() {
        List<ReportPeriod> periodList = new ArrayList<ReportPeriod>();
        periodList.add(reportPeriodDao.fetchOne(1));
        periodList.add(reportPeriodDao.fetchOne(2));
        Date startDate = new GregorianCalendar(2011, Calendar.JANUARY, 1).getTime();
        Date endDate = new GregorianCalendar(2014, Calendar.JANUARY, 10).getTime();
        List<ReportPeriod> actualPeriods = reportPeriodDao.getReportPeriodsByDate(startDate, endDate);
        assertEquals(periodList.get(0).getId(), actualPeriods.get(0).getId());
        assertEquals(periodList.get(1).getId(), actualPeriods.get(1).getId());
    }

    @Test
    public void getReportPeriodTypeById() {
	    assertEquals(reportPeriodDao.getReportPeriodTypeById(21L).getCode(), "99");
    }
}