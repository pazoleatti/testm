package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import junit.framework.Assert;
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
		FormTemplate ft = formTemplateDao.get(1);
		Assert.assertEquals(1, ft.getId().intValue());
		Assert.assertEquals(1, ft.getType().getId());
		Assert.assertEquals(false, ft.isNumberedColumns());
	}
	
	@Test(expected=DaoException.class)
	@Transactional
	public void testNotexistingGet() {
		formTemplateDao.get(-1000);
	}

}
