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
}
