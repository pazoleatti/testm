package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TaxPeriodDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TaxPeriodDaoTest {
	@Autowired
	private TaxPeriodDao taxPeriodDao;

	@Test
	public void getSuccessfulTest() {
		TaxPeriod taxPeriod = taxPeriodDao.get(1);
		assertEquals(1, taxPeriod.getId().intValue());
		assertEquals(TaxType.TRANSPORT, taxPeriod.getTaxType());
	}

	@Test(expected = DaoException.class)
	public void getNotExistentTest() {
		taxPeriodDao.get(-1);
	}

	@Test
	public void saveTest() {
		TaxPeriod taxPeriod = new TaxPeriod();
		taxPeriod.setTaxType(TaxType.INCOME);
		taxPeriod.setYear(2015);
		int id = taxPeriodDao.add(taxPeriod);
		assertEquals(TaxType.INCOME, taxPeriodDao.get(id).getTaxType());
		assertEquals(2015, taxPeriodDao.get(id).getYear());
	}

	@Test
	public void listAllPeriodsByTaxTypeSuccessfulTest() {
		List<TaxPeriod> taxPeriodList = taxPeriodDao.listByTaxType(TaxType.TRANSPORT);
		assertEquals(3, taxPeriodList.size());

		taxPeriodList = taxPeriodDao.listByTaxType(TaxType.VAT);
		assertEquals(1, taxPeriodList.size());

		taxPeriodList = taxPeriodDao.listByTaxType(TaxType.PROPERTY);
		assertEquals(1, taxPeriodList.size());

		taxPeriodList = taxPeriodDao.listByTaxType(TaxType.INCOME);
		assertEquals(2, taxPeriodList.size());
	}

	@Test
	public void getLastTest() {
		TaxPeriod lastTaxPeriod = taxPeriodDao.get(10);
		assertEquals(lastTaxPeriod.getYear(), taxPeriodDao.getLast(TaxType.TRANSPORT).getYear());
	}
}
