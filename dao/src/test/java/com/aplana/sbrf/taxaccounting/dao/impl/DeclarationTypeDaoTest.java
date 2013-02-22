package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationTypeDaoTest.xml"})
@Transactional
public class DeclarationTypeDaoTest {
	@Autowired
	private DeclarationTypeDao declarationTypeDao;
	
	@Test
	public void testGet() {
		DeclarationType dt = declarationTypeDao.get(1);
		assertEquals(1, dt.getId());
		assertEquals("Вид декларации (тест)", dt.getName());
		assertEquals(TaxType.TRANSPORT, dt.getTaxType());
	}
	
	@Test(expected=DaoException.class)
	public void testGetIncorrectId() {
		declarationTypeDao.get(1000);
	}

	@Test
	public void testListAllByTaxType() {
		assertEquals(4, declarationTypeDao.listAllByTaxType(TaxType.TRANSPORT).size());
	}

}
