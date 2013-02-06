package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ReportPeriodDaoTest.xml"})
@Transactional
public class ReportPeriodDaoTest {
	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Test
	public void getSuccessfulTest() {
		ReportPeriod reportPeriod = reportPeriodDao.get(1);
		assertEquals("Transport report period 1", reportPeriod.getName());
		assertTrue(reportPeriod.isActive());
		assertEquals(3, reportPeriod.getMonths());
		assertEquals(TaxType.TRANSPORT, reportPeriod.getTaxType());
	}

	@Test(expected = DaoException.class)
	public void getNotExistantTest() {
		reportPeriodDao.get(-1);
	}

	@Test
	public void getCurrentPeriodSuccessfulTest() {
		ReportPeriod reportPeriod = reportPeriodDao.getCurrentPeriod(TaxType.TRANSPORT);
		assertEquals("Transport report period 1", reportPeriod.getName());
		assertTrue(reportPeriod.isActive());
		assertEquals(TaxType.TRANSPORT, reportPeriod.getTaxType());
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
	public void listAllPeriodsByTaxTypeSuccessfulTest() {
		List<ReportPeriod> reportPeriodList = reportPeriodDao.listAllPeriodsByTaxType(TaxType.TRANSPORT);
		assertEquals(2, reportPeriodList.size());
		
		reportPeriodList = reportPeriodDao.listAllPeriodsByTaxType(TaxType.VAT);
		assertEquals(3, reportPeriodList.size());

		reportPeriodList = reportPeriodDao.listAllPeriodsByTaxType(TaxType.INCOME);
		assertEquals(1, reportPeriodList.size());
		
		reportPeriodList = reportPeriodDao.listAllPeriodsByTaxType(TaxType.PROPERTY);
		assertEquals(0, reportPeriodList.size());		
	}
}
