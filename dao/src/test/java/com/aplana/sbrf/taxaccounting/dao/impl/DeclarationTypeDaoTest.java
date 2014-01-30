package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
		assertEquals("Вид декларации 1", dt.getName());
		assertEquals(TaxType.TRANSPORT, dt.getTaxType());
	}
	
	@Test(expected=DaoException.class)
	public void testGetIncorrectId() {
		declarationTypeDao.get(1000);
	}
	
	@Test
	public void testListAll() {
		List<DeclarationType> list = declarationTypeDao.listAll();
		assertEquals(4, list.size());
	}

	@Test
	public void testListByTaxType() {
		List<DeclarationType> list = declarationTypeDao.listAllByTaxType(TaxType.TRANSPORT);
		assertEquals(2, list.size());
		
		for (DeclarationType dt: list) {
			assertEquals(TaxType.TRANSPORT, dt.getTaxType());
		}
	}

    @Test
    public void testGetByFilter(){
        TemplateFilter filter = new TemplateFilter();
        filter.setTaxType(TaxType.INCOME);
        filter.setActive(true);
        Assert.assertEquals(0, declarationTypeDao.getByFilter(filter).size());
        filter.setActive(false);
        Assert.assertEquals(0, declarationTypeDao.getByFilter(filter).size());
    }
}
