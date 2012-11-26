package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Vitalii Samolovskikh
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext.xml", "classpath:com/aplana/sbrf/taxaccounting/dao.xml"})
public class FormTemplateDaoTest {
	private FormTemplateDao formTemplateDao;

	@Test
	public void testTrue(){
		assert true;
	}

	/*
	@Test
	public void selectSave(){
		for (FormTemplate form:formTemplateDao.listAll()){
			formTemplateDao.save(form);
		}
	}
	*/

	@Autowired
	public void setFormTemplateDao(FormTemplateDao formTemplateDao) {
		this.formTemplateDao = formTemplateDao;
	}
}
