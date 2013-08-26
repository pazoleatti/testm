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
import org.springframework.dao.DuplicateKeyException;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DepartmentReportPeriodDaoImplTest.xml" })
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
	public void init() {

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
		reportPeriod2.setOrder(10);
		reportPeriod2.setMonths(3);
		reportPeriod2.setTaxPeriodId(taxPeriod.getId());
		reportPeriod2.setDictTaxPeriodId(21);
		reportPeriodDao.add(reportPeriod2);

	}

	@Test
	public void getNotExistentTest() {
		assertNull(departmentReportPeriodDao.get(reportPeriod1.getId(), -1l));
	}

	@Test
	public void getByDepartmentSuccessfulTest() {

		DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
		departmentReportPeriod.setDepartmentId(1l);
		departmentReportPeriod.setActive(true);
		departmentReportPeriod.setBalance(true);
		departmentReportPeriod.setReportPeriod(reportPeriod1);

		departmentReportPeriodDao.save(departmentReportPeriod);

		departmentReportPeriod = new DepartmentReportPeriod();
		departmentReportPeriod.setDepartmentId(1l);
		departmentReportPeriod.setActive(true);
		departmentReportPeriod.setBalance(true);
		departmentReportPeriod.setReportPeriod(reportPeriod2);

		departmentReportPeriodDao.save(departmentReportPeriod);

		List<DepartmentReportPeriod> reportPeriodList = departmentReportPeriodDao
				.getByDepartment(1l);
		assertEquals(2, reportPeriodList.size());
		assertEquals(9, reportPeriodList.get(0).getReportPeriod().getOrder());
		assertEquals(10, reportPeriodList.get(1).getReportPeriod().getOrder());

		reportPeriodList = departmentReportPeriodDao.getByDepartment(-1l);
		assertEquals(0, reportPeriodList.size());
	}

	@Test(expected = DuplicateKeyException.class)
	public void saveDublicatePKErrorTest() {

		DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
		departmentReportPeriod.setDepartmentId(1l);
		departmentReportPeriod.setActive(true);
		departmentReportPeriod.setBalance(true);
		departmentReportPeriod.setReportPeriod(reportPeriod1);

		departmentReportPeriodDao.save(departmentReportPeriod);

		departmentReportPeriod = new DepartmentReportPeriod();
		departmentReportPeriod.setDepartmentId(1l);
		departmentReportPeriod.setActive(true);
		departmentReportPeriod.setBalance(true);
		departmentReportPeriod.setReportPeriod(reportPeriod1);

		departmentReportPeriodDao.save(departmentReportPeriod);
	}

	@Test
	public void saveAndGetSuccessTest() {

		DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
		departmentReportPeriod.setDepartmentId(1l);
		departmentReportPeriod.setActive(true);
		departmentReportPeriod.setBalance(true);
		departmentReportPeriod.setReportPeriod(reportPeriod1);

		departmentReportPeriodDao.save(departmentReportPeriod);
		departmentReportPeriod = departmentReportPeriodDao.get(
				reportPeriod1.getId(), 1l);

		assertEquals(Long.valueOf(1l), departmentReportPeriod.getDepartmentId());
		assertEquals(true, departmentReportPeriod.isActive());
		assertEquals(true, departmentReportPeriod.isBalance());
		assertEquals(9, departmentReportPeriod.getReportPeriod().getOrder());
	}

	@Test
	public void updateActiveSuccessTest() {

		DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
		departmentReportPeriod.setDepartmentId(1l);
		departmentReportPeriod.setActive(true);
		departmentReportPeriod.setBalance(true);
		departmentReportPeriod.setReportPeriod(reportPeriod1);

		departmentReportPeriodDao.save(departmentReportPeriod);
		departmentReportPeriodDao.updateActive(departmentReportPeriod
				.getReportPeriod().getId(), 1l, false);
		departmentReportPeriod = departmentReportPeriodDao.get(
				reportPeriod1.getId(), 1l);

		assertEquals(Long.valueOf(1l), departmentReportPeriod.getDepartmentId());
		assertEquals(false, departmentReportPeriod.isActive());
		assertEquals(true, departmentReportPeriod.isBalance());
		assertEquals(9, departmentReportPeriod.getReportPeriod().getOrder());

		departmentReportPeriodDao.updateActive(departmentReportPeriod
				.getReportPeriod().getId(), 1l, true);
		departmentReportPeriod = departmentReportPeriodDao.get(
				reportPeriod1.getId(), 1l);

		assertEquals(true, departmentReportPeriod.isActive());
	}

}
