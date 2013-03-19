package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.script.TransportTaxDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vitalii Samolovskikh
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TransportTaxDaoTest.xml"})
public class TransportTaxDaoTest {
	@Autowired
	private TransportTaxDao transportTaxDao;

	@Test
	public void testSimple(){
		assertNotNull(transportTaxDao);
	}

	@Test(expected = DaoException.class)
	public void testInvalid(){
		transportTaxDao.getTaxRate("", BigDecimal.ZERO, BigDecimal.ZERO, "72");
	}

	@Test
	public void testValid(){
		assertEquals(7, transportTaxDao.getTaxRate("51015", BigDecimal.ZERO, BigDecimal.valueOf(99), "72"));
		assertEquals(7, transportTaxDao.getTaxRate("51015", BigDecimal.ZERO, BigDecimal.valueOf(100), "72"));
		
		assertEquals(20, transportTaxDao.getTaxRate("51015", BigDecimal.ZERO, BigDecimal.valueOf(120), "72"));
		assertEquals(75, transportTaxDao.getTaxRate("51015", BigDecimal.ZERO, BigDecimal.valueOf(250), "72"));
		assertEquals(150, transportTaxDao.getTaxRate("51015", BigDecimal.ZERO, BigDecimal.valueOf(300), "72"));

		assertEquals(7, transportTaxDao.getTaxRate("56100", BigDecimal.ZERO, BigDecimal.valueOf(20), "72"));
		assertEquals(15, transportTaxDao.getTaxRate("56100", BigDecimal.ZERO, BigDecimal.valueOf(21), "72"));
		assertEquals(15, transportTaxDao.getTaxRate("56100", BigDecimal.ZERO, BigDecimal.valueOf(35), "72"));

		assertEquals(10, transportTaxDao.getTaxRate("54011", BigDecimal.valueOf(4), BigDecimal.valueOf(110), "72"));
		assertEquals(17, transportTaxDao.getTaxRate("54011", BigDecimal.valueOf(4), BigDecimal.valueOf(111), "72"));
		assertEquals(35, transportTaxDao.getTaxRate("54011", BigDecimal.valueOf(4), BigDecimal.valueOf(201), "72"));

		assertEquals(10, transportTaxDao.getTaxRate("54011", BigDecimal.valueOf(5), BigDecimal.valueOf(110), "72"));
		assertEquals(17, transportTaxDao.getTaxRate("54011", BigDecimal.valueOf(5), BigDecimal.valueOf(111), "72"));
		assertEquals(35, transportTaxDao.getTaxRate("54011", BigDecimal.valueOf(5), BigDecimal.valueOf(201), "72"));

		assertEquals(15, transportTaxDao.getTaxRate("54011", BigDecimal.valueOf(6), BigDecimal.valueOf(110), "72"));
		assertEquals(26, transportTaxDao.getTaxRate("54011", BigDecimal.valueOf(6), BigDecimal.valueOf(111), "72"));
		assertEquals(55, transportTaxDao.getTaxRate("54011", BigDecimal.valueOf(6), BigDecimal.valueOf(201), "72"));
	}
}
