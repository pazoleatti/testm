package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.joda.time.LocalDateTime;
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
		reportPeriodDao.get(-1);
	}
	
	@Before
	public void init(){
		taxPeriod = new TaxPeriod();
		taxPeriod.setYear(Calendar.getInstance().get(Calendar.YEAR));
		taxPeriod.setId(taxPeriodDao.add(taxPeriod));
	}


	@Test
	public void listByTaxPeriodSuccessfulTest() {
		ReportPeriod newReportPeriod = new ReportPeriod();
		newReportPeriod.setName("MyTestName1");
		newReportPeriod.setTaxPeriod(taxPeriod);
		newReportPeriod.setDictTaxPeriodId(21);
		newReportPeriod.setStartDate(new LocalDateTime());
		newReportPeriod.setEndDate(new LocalDateTime());
		newReportPeriod.setCalendarStartDate(new LocalDateTime(new GregorianCalendar(2014,Calendar.JANUARY,1).getTime()));
		reportPeriodDao.save(newReportPeriod);
		
		newReportPeriod = new ReportPeriod();
		newReportPeriod.setName("MyTestName2");
		newReportPeriod.setTaxPeriod(taxPeriod);
		newReportPeriod.setDictTaxPeriodId(22);
		newReportPeriod.setStartDate(new LocalDateTime());
		newReportPeriod.setEndDate(new LocalDateTime());
		newReportPeriod.setCalendarStartDate(new LocalDateTime(2014,Calendar.JANUARY+1,1, 0, 0));
		reportPeriodDao.save(newReportPeriod);
		
		List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(taxPeriod.getId());
        assertEquals(2, reportPeriodList.size());

		reportPeriodList = reportPeriodDao.listByTaxPeriod(-1);
		assertEquals(0, reportPeriodList.size());
	}

	@Test
	public void saveAndGetSuccessTest() {
		ReportPeriod newReportPeriod = new ReportPeriod();
		newReportPeriod.setName("MyTestName");
		newReportPeriod.setTaxPeriod(taxPeriod);
		newReportPeriod.setDictTaxPeriodId(21);
		newReportPeriod.setStartDate(new LocalDateTime());
		newReportPeriod.setEndDate(new LocalDateTime());
		newReportPeriod.setCalendarStartDate(new LocalDateTime(2014, Calendar.JANUARY+1, 1,0, 0));

		int newReportPeriodId = reportPeriodDao.save(newReportPeriod);
		ReportPeriod reportPeriod = reportPeriodDao.get(newReportPeriodId);

		assertEquals("MyTestName", reportPeriod.getName());
		assertEquals(taxPeriod.getId(), reportPeriod.getTaxPeriod().getId());
		assertEquals(taxPeriod.getId(), reportPeriod.getTaxPeriod().getId());
		assertEquals(21, reportPeriod.getDictTaxPeriodId());
	}

    @Test
    public void getReportPeriodByTaxPeriodAndDictTest1() {
        ReportPeriod reportPeriod1 = reportPeriodDao.getByTaxPeriodAndDict(1, 21);
        ReportPeriod reportPeriod2 = reportPeriodDao.getByTaxPeriodAndDict(1, 22);
        Assert.assertEquals(reportPeriod1.getId(), Integer.valueOf(1));
        Assert.assertEquals(reportPeriod2.getId(), Integer.valueOf(2));
    }

    @Test(expected = DaoException.class)
    public void getReportPeriodByTaxPeriodAndDictTest2() {
        reportPeriodDao.getByTaxPeriodAndDict(-1, -1);
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
        reportPeriods = reportPeriodDao.getPeriodsByTaxTypeAndDepartments(TaxType.NDFL, asList(1, 2, 3));
        Assert.assertEquals(3, reportPeriods.size());
        Assert.assertTrue(getReportPeriodIds(reportPeriods).containsAll(asList(1, 2, 3)));
            }

    private ReportPeriod getReportPeriod() {
        ReportPeriod newReportPeriod = new ReportPeriod();
        newReportPeriod.setName("MyTestName");
        newReportPeriod.setTaxPeriod(taxPeriod);
        newReportPeriod.setDictTaxPeriodId(21);
        newReportPeriod.setStartDate(new LocalDateTime());
        newReportPeriod.setEndDate(new LocalDateTime());
        return newReportPeriod;
    }

    @Test(expected = DaoException.class)
    public void calendarStartDateTest1() {
        ReportPeriod newReportPeriod = getReportPeriod();
        newReportPeriod.setCalendarStartDate(new LocalDateTime(2014, Calendar.JANUARY+1, 25,0, 0));
        int newReportPeriodId = reportPeriodDao.save(newReportPeriod);
        reportPeriodDao.get(newReportPeriodId);
    }

    @Test(expected = DaoException.class)
    public void calendarStartDateTest2() {
        ReportPeriod newReportPeriod = getReportPeriod();
        newReportPeriod.setCalendarStartDate(new LocalDateTime(2014, Calendar.FEBRUARY+1, 1, 0, 0));
        int newReportPeriodId = reportPeriodDao.save(newReportPeriod);
        reportPeriodDao.get(newReportPeriodId);
    }

    @Test(expected = DaoException.class)
    public void calendarStartDateTest3() {
        ReportPeriod newReportPeriod = getReportPeriod();
        newReportPeriod.setCalendarStartDate(new LocalDateTime(2014, Calendar.MARCH+1, 10,0,0));
        int newReportPeriodId = reportPeriodDao.save(newReportPeriod);
        reportPeriodDao.get(newReportPeriodId);
    }

    @Test
    public void calendarStartDateTest4() {
        ReportPeriod newReportPeriod = getReportPeriod();
        LocalDateTime date = new LocalDateTime(2014, Calendar.APRIL+1, 1,0 ,0);
        newReportPeriod.setCalendarStartDate(date);
        int newReportPeriodId = reportPeriodDao.save(newReportPeriod);
        ReportPeriod reportPeriod = reportPeriodDao.get(newReportPeriodId);
        assertEquals(date, reportPeriod.getCalendarStartDate());
        assertEquals(2, reportPeriod.getOrder());
    }

    @Test
    public void getByTaxTypedCodeYearTest() {
        ReportPeriod reportPeriod1 = reportPeriodDao.getByTaxTypedCodeYear(TaxType.NDFL, "21", 2013);
        ReportPeriod reportPeriod2 = reportPeriodDao.getByTaxTypedCodeYear(TaxType.NDFL, "99", 2015);
        Assert.assertNull(reportPeriod1);
        Assert.assertNotNull(reportPeriod2);
        Assert.assertEquals(3, reportPeriod2.getId().intValue());
    }

    @Test
    public void getReportPeriodsByDateTest() {
        List<ReportPeriod> periodList = new ArrayList<ReportPeriod>();
        periodList.add(reportPeriodDao.get(1));
        periodList.add(reportPeriodDao.get(2));
        LocalDateTime startDate = new LocalDateTime(2011, Calendar.JANUARY+1, 1, 0, 0);
        LocalDateTime endDate = new LocalDateTime(2014, Calendar.JANUARY+1, 10, 0,0);
        List<ReportPeriod> actualPeriods = reportPeriodDao.getReportPeriodsByDate(TaxType.NDFL, startDate, endDate);
        Assert.assertEquals(periodList.get(0).getId(), actualPeriods.get(0).getId());
        Assert.assertEquals(periodList.get(1).getId(), actualPeriods.get(1).getId());
    }

    @Test
    public void getReportPeriodsByDateAndDepartmentTest() {
        LocalDateTime startDate = new LocalDateTime(2011, Calendar.JANUARY+1, 1,0,0);
        LocalDateTime endDate = new LocalDateTime(2014, Calendar.JANUARY+1, 10,0,0);
        assertEquals(2, reportPeriodDao.getReportPeriodsByDateAndDepartment(TaxType.NDFL, 1, startDate, endDate).size());
    }
}