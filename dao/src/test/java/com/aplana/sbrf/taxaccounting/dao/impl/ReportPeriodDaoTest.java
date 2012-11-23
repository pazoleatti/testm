package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

//TODO: переработать, чтобы не было необходимости поднимать полный файл dao.xml, а то получается integration-тест вместо unit-теста
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext.xml", "classpath:com/aplana/sbrf/taxaccounting/dao.xml"})
public class ReportPeriodDaoTest {
	/*
	* Тесты основываются на том, что в БД уже лежит отчетный период со значениями id = 1 tax_type = 'T' и is_active = 1
	* TODO: переписать тесты, когда будут реализованы методы сохранения отчетных периодов
	*/

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Test
	public void getSuccessfulTest(){
		ReportPeriod reportPeriod = reportPeriodDao.get(1);
		assertEquals(reportPeriod.getName(), "Отчетный период 1");
		assertEquals(reportPeriod.isActive(), true);
		assertEquals(reportPeriod.getTaxType(), TaxType.TRANSPORT);
	}

	@Test(expected = DaoException.class)
	public void getDaoExceptionTest(){
		reportPeriodDao.get(1000);
	}

	@Test
	public void getCurrentPeriodSuccessfulTest(){
	 	ReportPeriod reportPeriod = reportPeriodDao.getCurrentPeriod(TaxType.TRANSPORT);
		assertEquals(reportPeriod.getId(), 1);
		assertEquals(reportPeriod.getName(), "Отчетный период 1");
		assertEquals(reportPeriod.isActive(), true);
	}

	@Test
	public void getCurrentPeriodNullResultTest(){
		ReportPeriod reportPeriod = reportPeriodDao.getCurrentPeriod(TaxType.VAT);
		assertEquals(reportPeriod, null);
	}

	@Test
	public void listAllPeriodsByTaxTypeSuccessfulTest(){
		List<ReportPeriod> reportPeriodList = reportPeriodDao.listAllPeriodsByTaxType(TaxType.TRANSPORT);
		assertEquals(reportPeriodList.size(), 1);
		assertEquals(reportPeriodList.get(0).getId(), 1);
		assertEquals(reportPeriodList.get(0).getName(), "Отчетный период 1");
		assertEquals(reportPeriodList.get(0).getTaxType(), TaxType.TRANSPORT);
		assertEquals(reportPeriodList.get(0).isActive(), true);
	}

	@Test
	public void listAllPeriodsByTaxTypeNullResultTest(){
		List<ReportPeriod> reportPeriodList = reportPeriodDao.listAllPeriodsByTaxType(TaxType.VAT);
		assertEquals(reportPeriodList.size(), 0);
	}
}
