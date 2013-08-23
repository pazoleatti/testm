package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentReportPeriodDaoImplTest.xml"})
@Transactional
public class DepartmentReportPeriodDaoImplTest {
	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Autowired
	private TaxPeriodDao taxPeriodDao;
	
	@Autowired
	private DepartmentReportPeriodDao departmentReportPeriodDao;
	
	private TaxPeriod taxPeriod;
	
	private ReportPeriod reportPeriod1;
	
	private ReportPeriod reportPeriod2;
		
	@Before
	public void init(){
		
		taxPeriod = new TaxPeriod();
		taxPeriod.setStartDate(new Date());
		taxPeriod.setEndDate(new Date());
		taxPeriod.setTaxType(TaxType.TRANSPORT);
		taxPeriodDao.add(taxPeriod);
		
		reportPeriod1 = new ReportPeriod();
		reportPeriod1.setName("MyTestName1");
		reportPeriod1.setOrder(9);
		reportPeriod1.setMonths(3);
		reportPeriod1.setTaxPeriodId(taxPeriod.getId());
		reportPeriod1.setDictTaxPeriodId(21);
		reportPeriodDao.add(reportPeriod1);

		reportPeriod2 = new ReportPeriod();
		reportPeriod2.setName("MyTestName1");
		reportPeriod2.setOrder(9);
		reportPeriod2.setMonths(3);
		reportPeriod2.setTaxPeriodId(taxPeriod.getId());
		reportPeriod2.setDictTaxPeriodId(21);
		reportPeriodDao.add(reportPeriod2);
			
	}

	public void getNotExistentTest() {
		assertNull(departmentReportPeriodDao.get(reportPeriod1.getId(), -1l));
	}
	
	@Test
	public void listByTaxPeriodSuccessfulTest() {
		
		TaxPeriod taxPeriod = new TaxPeriod();
		taxPeriod.setStartDate(new Date());
		taxPeriod.setEndDate(new Date());
		taxPeriod.setTaxType(TaxType.TRANSPORT);
		taxPeriodDao.add(taxPeriod);
		
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
		
		DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
		departmentReportPeriod.setDepartmentId(1l);
		departmentReportPeriod.setActive(true);
		departmentReportPeriod.setBalance(true);
		departmentReportPeriod.setReportPeriod(reportPeriod1);
		
		departmentReportPeriodDao.save(departmentReportPeriod);
		departmentReportPeriod = departmentReportPeriodDao.get(reportPeriod1.getId(), 1l);
		

		assertEquals(Long.valueOf(1l), departmentReportPeriod.getDepartmentId());
		assertEquals(true, departmentReportPeriod.isActive());
		assertEquals(true, departmentReportPeriod.isBalance());
		assertEquals(9, departmentReportPeriod.getReportPeriod().getOrder());
	}

}
