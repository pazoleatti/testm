package com.aplana.sbrf.taxaccounting.dao.impl;

import java.util.List;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ReportPeriodDaoTest.xml"})
@Transactional
public class ReportPeriodDaoTest {
	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Autowired
	private TaxPeriodDao taxPeriodDao;

	@Test
	public void getSuccessfulTest() {
		ReportPeriod reportPeriod = reportPeriodDao.get(1);
		assertEquals("Transport report period 1", reportPeriod.getName());
		assertTrue(reportPeriod.isActive());
		assertEquals(3, reportPeriod.getMonths());
		assertEquals(TaxType.TRANSPORT, taxPeriodDao.get(reportPeriod.getTaxPeriodId()).getTaxType());
	}

	@Test(expected = DaoException.class)
	public void getNotExistentTest() {
		reportPeriodDao.get(-1);
	}

	@Test
	public void getCurrentPeriodSuccessfulTest() {
		ReportPeriod reportPeriod = reportPeriodDao.getCurrentPeriod(TaxType.TRANSPORT);
		assertEquals("Transport report period 1", reportPeriod.getName());
		assertTrue(reportPeriod.isActive());
		assertEquals(TaxType.TRANSPORT, taxPeriodDao.get(reportPeriod.getTaxPeriodId()).getTaxType());
	}

	@Test
	public void getCurrentPeriodEmptyTest() {
		ReportPeriod reportPeriod = reportPeriodDao.getCurrentPeriod(TaxType.INCOME);
		assertNull(reportPeriod);
	}
	
	@Test(expected=DaoException.class)
	public void getCurrentPeriodDuplicateTest() {
		reportPeriodDao.getCurrentPeriod(TaxType.VAT);
	}

	@Test
	public void listByTaxPeriodSuccessfulTest() {
		List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(1);
		assertEquals(2, reportPeriodList.size());
		assertEquals(1, reportPeriodList.get(0).getOrder());
		assertEquals(2, reportPeriodList.get(1).getOrder());

		reportPeriodList = reportPeriodDao.listByTaxPeriod(21);
		assertEquals(3, reportPeriodList.size());
		assertEquals(1, reportPeriodList.get(0).getOrder());
		assertEquals(2, reportPeriodList.get(1).getOrder());
		assertEquals(3, reportPeriodList.get(2).getOrder());

		reportPeriodList = reportPeriodDao.listByTaxPeriod(31);
		assertEquals(1, reportPeriodList.size());
		assertEquals(1, reportPeriodList.get(0).getOrder());

		reportPeriodList = reportPeriodDao.listByTaxPeriod(-1);
		assertEquals(0, reportPeriodList.size());
	}

	@Test
	public void closeReportPeriodTest() {
		ReportPeriod reportPeriod = reportPeriodDao.get(1);
		assertEquals(true, reportPeriod.isActive());
		reportPeriodDao.changeActive(reportPeriod.getId(), false);
		reportPeriod = reportPeriodDao.get(1);
		assertEquals(false, reportPeriod.isActive());
	}

	@Test
	public void openReportPeriodTest() {
		ReportPeriod reportPeriod = reportPeriodDao.get(2);
		assertEquals(false, reportPeriod.isActive());
		reportPeriodDao.changeActive(reportPeriod.getId(), true);
		reportPeriod = reportPeriodDao.get(2);
		assertEquals(true, reportPeriod.isActive());
	}

	@Test
	public void addTest() {
		ReportPeriod newReportPeriod = new ReportPeriod();
		newReportPeriod.setBalancePeriod(true);
		newReportPeriod.setName("MyTestName");
		newReportPeriod.setOrder(reportPeriodDao.listByTaxPeriod(1).size() + 1);
		newReportPeriod.setActive(true);
		newReportPeriod.setMonths(3);
		newReportPeriod.setDepartmentId(2);
		newReportPeriod.setTaxPeriodId(1);
		newReportPeriod.setDictTaxPeriodId(21);

		int newReportPeriodId = reportPeriodDao.add(newReportPeriod);

		ReportPeriod reportPeriod = reportPeriodDao.get(newReportPeriodId);

		assertEquals(true, reportPeriod.isBalancePeriod());
		assertEquals("MyTestName", reportPeriod.getName());
		assertEquals(true, reportPeriod.isActive());
		assertEquals(3, reportPeriod.getMonths());
		assertEquals(2, reportPeriod.getDepartmentId());
		assertEquals(1, reportPeriod.getTaxPeriodId());
	}

    @Test
    public void getLastReportPeriodTest() {
        ReportPeriod period = reportPeriodDao.getLastReportPeriod(TaxType.TRANSPORT, 1L);
        assertNotNull(period);
    }
}
