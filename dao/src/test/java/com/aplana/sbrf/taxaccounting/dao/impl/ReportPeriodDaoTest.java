package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ReportPeriodDaoTest.xml"})
@Transactional
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
		taxPeriod.setStartDate(new Date());
		taxPeriod.setEndDate(new Date());
		taxPeriod.setTaxType(TaxType.TRANSPORT);
		taxPeriodDao.add(taxPeriod);
	}
	
	@Test
	public void listByTaxPeriodSuccessfulTest() {
		

		
		ReportPeriod newReportPeriod = new ReportPeriod();
		newReportPeriod.setName("MyTestName1");
		newReportPeriod.setOrder(9);
		newReportPeriod.setMonths(3);
		newReportPeriod.setTaxPeriodId(taxPeriod.getId());
		newReportPeriod.setDictTaxPeriodId(21);
		reportPeriodDao.add(newReportPeriod);
		
		newReportPeriod = new ReportPeriod();
		newReportPeriod.setName("MyTestName2");
		newReportPeriod.setOrder(10);
		newReportPeriod.setMonths(3);
		newReportPeriod.setTaxPeriodId(taxPeriod.getId());
		newReportPeriod.setDictTaxPeriodId(21);
		reportPeriodDao.add(newReportPeriod);
		
		List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(taxPeriod.getId());
		assertEquals(2, reportPeriodList.size());
		assertEquals(9, reportPeriodList.get(0).getOrder());
		assertEquals(10, reportPeriodList.get(1).getOrder());

		reportPeriodList = reportPeriodDao.listByTaxPeriod(-1);
		assertEquals(0, reportPeriodList.size());
	}


	@Test
	public void saveAndGetSuccessTest() {
		ReportPeriod newReportPeriod = new ReportPeriod();
		newReportPeriod.setName("MyTestName");
		newReportPeriod.setOrder(9);
		newReportPeriod.setMonths(3);
		newReportPeriod.setTaxPeriodId(taxPeriod.getId());
		newReportPeriod.setDictTaxPeriodId(21);

		int newReportPeriodId = reportPeriodDao.add(newReportPeriod);
		ReportPeriod reportPeriod = reportPeriodDao.get(newReportPeriodId);

		assertEquals("MyTestName", reportPeriod.getName());
		assertEquals(3, reportPeriod.getMonths());
		assertEquals(taxPeriod.getId(), Integer.valueOf(reportPeriod.getTaxPeriodId()));
		assertEquals(9, reportPeriod.getOrder());
		assertEquals(taxPeriod.getId(), Integer.valueOf(reportPeriod.getTaxPeriodId()));
		assertEquals(21, reportPeriod.getDictTaxPeriodId());
	}

}
