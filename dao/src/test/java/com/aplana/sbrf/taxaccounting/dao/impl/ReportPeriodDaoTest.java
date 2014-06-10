package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
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
		taxPeriod.setTaxType(TaxType.TRANSPORT);
		taxPeriod.setYear(Calendar.getInstance().get(Calendar.YEAR));
		taxPeriodDao.add(taxPeriod);
	}

    @Test
    public void getCorrectPeriods() {
        reportPeriodDao.getCorrectPeriods(TaxType.DEAL, 1);
    }
	
	@Test
	public void listByTaxPeriodSuccessfulTest() {
		ReportPeriod newReportPeriod = new ReportPeriod();
		newReportPeriod.setName("MyTestName1");
		newReportPeriod.setTaxPeriod(taxPeriod);
		newReportPeriod.setDictTaxPeriodId(21);
		newReportPeriod.setStartDate(new Date());
		newReportPeriod.setEndDate(new Date());
		newReportPeriod.setCalendarStartDate(new Date());
		reportPeriodDao.save(newReportPeriod);
		
		newReportPeriod = new ReportPeriod();
		newReportPeriod.setName("MyTestName2");
		newReportPeriod.setTaxPeriod(taxPeriod);
		newReportPeriod.setDictTaxPeriodId(22);
		newReportPeriod.setStartDate(new Date());
		newReportPeriod.setEndDate(new Date());
		newReportPeriod.setCalendarStartDate(new Date());
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
		newReportPeriod.setStartDate(new Date());
		newReportPeriod.setEndDate(new Date());
		newReportPeriod.setCalendarStartDate(new Date());

		int newReportPeriodId = reportPeriodDao.save(newReportPeriod);
		ReportPeriod reportPeriod = reportPeriodDao.get(newReportPeriodId);

		assertEquals("MyTestName", reportPeriod.getName());
		assertEquals(taxPeriod.getId(), Integer.valueOf(reportPeriod.getTaxPeriod().getId()));
		assertEquals(taxPeriod.getId(), Integer.valueOf(reportPeriod.getTaxPeriod().getId()));
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
        reportPeriods = reportPeriodDao.getPeriodsByTaxTypeAndDepartments(TaxType.INCOME, asList(1, 2, 3));
        Assert.assertEquals(0, reportPeriods.size());
        reportPeriods = reportPeriodDao.getPeriodsByTaxTypeAndDepartments(TaxType.TRANSPORT, asList(1, 2, 3));
        Assert.assertEquals(2, reportPeriods.size());
        Assert.assertTrue(getReportPeriodIds(reportPeriods).containsAll(asList(1, 2)));
        reportPeriods = reportPeriodDao.getPeriodsByTaxTypeAndDepartments(TaxType.TRANSPORT, asList(3));
        Assert.assertEquals(0, reportPeriods.size());
    }

    @Test
    public void getPeriodsByTaxTypesAndDepartmentsTest() {
        List<Long> reportPeriods;
        reportPeriods = reportPeriodDao.getPeriodsByTaxTypesAndDepartments(Arrays.asList(TaxType.INCOME, TaxType.TRANSPORT), asList(1, 2, 3));
        Assert.assertEquals(2, reportPeriods.size());
        reportPeriods = reportPeriodDao.getPeriodsByTaxTypesAndDepartments(Arrays.asList(TaxType.INCOME, TaxType.TRANSPORT), asList(1000));
        Assert.assertEquals(0, reportPeriods.size());
    }
}
