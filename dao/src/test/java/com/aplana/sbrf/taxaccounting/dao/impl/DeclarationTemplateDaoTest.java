package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DeclarationTemplateDaoTest.xml"})
@Transactional
public class DeclarationTemplateDaoTest {
	@Autowired
	private DeclarationTemplateDao declarationTemplateDao;

	@Test
	public void testListAll() {
		assertEquals(5, declarationTemplateDao.listAll().size());
	}

	@Test
	public void testGet() {
		DeclarationTemplate d1 = declarationTemplateDao.get(1);
		assertEquals(1, d1.getId().longValue());
		assertEquals('T', d1.getTaxType().getCode());
		assertEquals("0.01", d1.getVersion());
		assertTrue(d1.isActive());

		DeclarationTemplate d2 = declarationTemplateDao.get(2);
		assertEquals(2, d2.getId().longValue());
		assertEquals('T', d2.getTaxType().getCode());
		assertEquals("0.01", d2.getVersion());
		assertTrue(d2.isActive());
	}
	
	@Test(expected=DaoException.class)
	public void testGetNotExisted() {
		declarationTemplateDao.get(1000);
	}

	@Test
	public void testSaveNew() {
		DeclarationTemplate declarationTemplate = new DeclarationTemplate();
		declarationTemplate.setActive(true);
		declarationTemplate.setVersion("0.01");
		declarationTemplate.setCreateScript("MyScript");
		declarationTemplate.setTaxType(TaxType.TRANSPORT);

		int id = declarationTemplateDao.save(declarationTemplate);

		DeclarationTemplate savedDeclarationTemplate = declarationTemplateDao.get(id);
		assertEquals(id, savedDeclarationTemplate.getId().intValue());
		assertEquals("0.01", savedDeclarationTemplate.getVersion());
		assertEquals("MyScript", savedDeclarationTemplate.getCreateScript());
		assertEquals(TaxType.TRANSPORT, savedDeclarationTemplate.getTaxType());
		assertTrue(savedDeclarationTemplate.isActive());

	}

	@Test
	public void testSaveExist() {
		DeclarationTemplate declarationTemplate = new DeclarationTemplate();
		declarationTemplate.setId(1);
		declarationTemplate.setEdition(1);
		declarationTemplate.setActive(true);
		declarationTemplate.setVersion("0.01");
		declarationTemplate.setCreateScript("MyScript");
		declarationTemplate.setTaxType(TaxType.TRANSPORT);

		declarationTemplateDao.save(declarationTemplate);

		DeclarationTemplate savedDeclarationTemplate = declarationTemplateDao.get(1);
		assertEquals(1, savedDeclarationTemplate.getId().intValue());
		assertEquals("0.01", savedDeclarationTemplate.getVersion());
		assertEquals("MyScript", savedDeclarationTemplate.getCreateScript());
		assertEquals(TaxType.TRANSPORT, savedDeclarationTemplate.getTaxType());
		assertTrue(savedDeclarationTemplate.isActive());
	}

	@Test(expected = DaoException.class)
	public void testSaveExistWithBadEdition() {
		DeclarationTemplate declarationTemplate = new DeclarationTemplate();
		declarationTemplate.setId(1);
		declarationTemplate.setEdition(1000);
		declarationTemplate.setActive(true);
		declarationTemplate.setVersion("0.01");
		declarationTemplate.setCreateScript("MyScript");
		declarationTemplate.setTaxType(TaxType.TRANSPORT);

		declarationTemplateDao.save(declarationTemplate);

	}

	@Test
	public void testSetJrxmlAndJasper() {
		declarationTemplateDao.setJrxmlAndJasper(1, "Template", new byte[]{00,01,02});
		assertEquals("Template", declarationTemplateDao.getJrxml(1));
		assertNotNull(declarationTemplateDao.getJasper(1));
	}

	@Test(expected = DaoException.class)
	public void testSetJrxmlAndJasperNotExisted() {
		declarationTemplateDao.setJrxmlAndJasper(1000, "Template", new byte[]{00,01,02});
	}

	@Test
	public void testGetJrxml() {
		assertEquals("test-jrxml", declarationTemplateDao.getJrxml(1));
	}

	@Test(expected = DaoException.class)
	public void testGetJrxmlNotExisted() {
		declarationTemplateDao.getJrxml(1000);
	}

	@Test(expected = DaoException.class)
	public void testGetJasperNotExisted() {
		declarationTemplateDao.getJasper(1000);
	}
}