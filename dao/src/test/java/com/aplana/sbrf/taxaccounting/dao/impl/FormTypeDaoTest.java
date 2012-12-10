package com.aplana.sbrf.taxaccounting.dao.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

//TODO: переработать, чтобы не было необходимости поднимать полный файл dao.xml, а то получается integration-тест вместо unit-теста 
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext.xml", "classpath:com/aplana/sbrf/taxaccounting/dao.xml"})
public class FormTypeDaoTest {
	@Autowired
	private FormTypeDao formTypeDao;
	
	@Test
	public void testGet() {
		FormType t = formTypeDao.getType(Constants.DEMO_FORM_TYPE_ID);
		Assert.assertEquals("DEMO", t.getName());
		Assert.assertEquals(Constants.DEMO_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.VAT, t.getTaxType());
	}
	
	@Test(expected=DaoException.class)
	public void testWrongIdGet() {
		formTypeDao.getType(-1000);
	}
}
