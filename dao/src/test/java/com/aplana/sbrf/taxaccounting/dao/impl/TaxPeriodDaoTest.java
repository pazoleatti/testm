package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.model.TaxType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TaxPeriodDaoTest.xml"})
@Transactional
public class TaxPeriodDaoTest {
	@Autowired
	private TaxPeriodDao taxPeriodDao;

	@Test
	public void getSuccessfulTest() {
		TaxPeriod taxPeriod = taxPeriodDao.get(1);
		assertEquals(1, taxPeriod.getId().intValue());
		assertEquals(TaxType.TRANSPORT, taxPeriod.getTaxType());
		assertEquals(getDate(2013, 0, 1), taxPeriod.getStartDate());
		assertEquals(getDate(2013, 11, 31), taxPeriod.getEndDate());
	}

	@Test(expected = DaoException.class)
	public void getNotExistentTest() {
		taxPeriodDao.get(-1);
	}

	@Test
	public void saveTest() {
		TaxPeriod taxPeriod = new TaxPeriod();
		taxPeriod.setTaxType(TaxType.INCOME);
		Date date = getDate(2013, 11, 12);
		taxPeriod.setStartDate(date);
		taxPeriod.setEndDate(date);
		int id = taxPeriodDao.add(taxPeriod);
		assertEquals(TaxType.INCOME, taxPeriodDao.get(id).getTaxType());
		assertEquals(date, taxPeriodDao.get(id).getStartDate());
		assertEquals(date, taxPeriodDao.get(id).getEndDate());
	}

	@Test
	public void listAllPeriodsByTaxTypeSuccessfulTest() {
		List<TaxPeriod> taxPeriodList = taxPeriodDao.listByTaxType(TaxType.TRANSPORT);
		assertEquals(2, taxPeriodList.size());

		taxPeriodList = taxPeriodDao.listByTaxType(TaxType.VAT);
		assertEquals(1, taxPeriodList.size());

		taxPeriodList = taxPeriodDao.listByTaxType(TaxType.PROPERTY);
		assertEquals(1, taxPeriodList.size());

		taxPeriodList = taxPeriodDao.listByTaxType(TaxType.INCOME);
		assertEquals(0, taxPeriodList.size());
	}

	@Test
	public void getLastTest() {
		TaxPeriod lastTaxPeriod = taxPeriodDao.get(1);
		assertEquals(lastTaxPeriod.getStartDate(), taxPeriodDao.getLast(TaxType.TRANSPORT).getStartDate());
	}

	@Test
	public void listByTaxTypeAndDateTest() {
		Date d2010 = getDate(2010, 0, 1);
		Date d2012_2 = getDate(2012, 5, 1);
		Date d2013 = getDate(2013, 0, 1);
		Date d2013_2 = getDate(2013, 5, 1);
		Date d2015 = getDate(2015, 0, 1);
		List<TaxPeriod> taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(TaxType.TRANSPORT, d2010, d2015);
		assertEquals(2, taxPeriodList.size());
		taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(TaxType.INCOME, d2010, d2015);
		assertEquals(0, taxPeriodList.size());
		taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(TaxType.DEAL, d2010, d2015);
		assertEquals(0, taxPeriodList.size());
		taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(TaxType.PROPERTY, d2010, d2015);
		assertEquals(1, taxPeriodList.size());
		taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(TaxType.VAT, d2010, d2015);
		assertEquals(1, taxPeriodList.size());

		taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(TaxType.TRANSPORT, d2012_2, d2012_2);
		assertEquals(1, taxPeriodList.size());
		taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(TaxType.TRANSPORT, d2012_2, d2013_2);
		assertEquals(2, taxPeriodList.size());
		taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(TaxType.TRANSPORT, d2013, d2013);
		assertEquals(1, taxPeriodList.size());
		taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(TaxType.TRANSPORT, d2010, d2010);
		assertEquals(0, taxPeriodList.size());
		taxPeriodList = taxPeriodDao.listByTaxTypeAndDate(TaxType.TRANSPORT, d2015, d2015);
		assertEquals(0, taxPeriodList.size());
	}

	private Date getDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
