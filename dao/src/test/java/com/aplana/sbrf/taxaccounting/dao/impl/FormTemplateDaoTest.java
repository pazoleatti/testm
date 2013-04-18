package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Vitalii Samolovskikh
 * 			Damir Sultanbekov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormTemplateDaoTest.xml"})
public class FormTemplateDaoTest {
	// TODO: расширить тесты
	@Autowired
	private FormTemplateDao formTemplateDao;

	@Test
	@Transactional
	public void testGet(){
		FormTemplate ft1 = formTemplateDao.get(1);
		Assert.assertEquals(1, ft1.getId().intValue());
		Assert.assertEquals(1, ft1.getType().getId());
		Assert.assertEquals(true, ft1.isActive());
		Assert.assertFalse(ft1.isNumberedColumns());
		Assert.assertTrue(ft1.isFixedRows());
		Assert.assertEquals("name_1", ft1.getName());
		Assert.assertEquals("fullname_1", ft1.getFullName());
		Assert.assertEquals("code_1", ft1.getCode());

		FormTemplate ft2 = formTemplateDao.get(2);
		Assert.assertEquals(2, ft2.getId().intValue());
		Assert.assertEquals(2, ft2.getType().getId());
		Assert.assertEquals(false, ft2.isActive());
		Assert.assertTrue(ft2.isNumberedColumns());
		Assert.assertFalse(ft2.isFixedRows());
		Assert.assertEquals("name_2", ft2.getName());
		Assert.assertEquals("fullname_2", ft2.getFullName());
		Assert.assertEquals("code_2", ft2.getCode());
	}
	
	@Test(expected=DaoException.class)
	@Transactional
	public void testNotexistingGet() {
		formTemplateDao.get(-1000);
	}

	@Test
	@Transactional
	public void testSave() {
		FormTemplate formTemplate = formTemplateDao.get(1);
		formTemplate.setNumberedColumns(true);
		formTemplate.setFixedRows(false);
		formTemplate.setVersion("321");
		formTemplate.setActive(true);
		formTemplate.setName("name_3");
		formTemplate.setFullName("fullname_3");
		formTemplate.setCode("code_3");
		formTemplateDao.save(formTemplate);
		formTemplate = formTemplateDao.get(1);
		Assert.assertTrue(formTemplate.isNumberedColumns());
		Assert.assertFalse(formTemplate.isFixedRows());
		Assert.assertEquals("321", formTemplate.getVersion());
		Assert.assertEquals(true, formTemplate.isActive());
		Assert.assertEquals("name_3", formTemplate.getName());
		Assert.assertEquals("fullname_3", formTemplate.getFullName());
		Assert.assertEquals("code_3", formTemplate.getCode());
	}

}
