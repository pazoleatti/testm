package com.aplana.sbrf.taxaccounting.dao.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.DaoException;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormTypeDaoTest.xml"})
@Transactional
public class FormTypeDaoTest {
	@Autowired
	private FormTypeDao formTypeDao;
	
	@Test
	public void testGet() {
		FormType t = formTypeDao.getType(Constants.TRANSPORT_FORM_TYPE_ID);
		Assert.assertEquals("FormType - Transport", t.getName());
		Assert.assertEquals(Constants.TRANSPORT_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.TRANSPORT, t.getTaxType());
		
		t = formTypeDao.getType(Constants.INCOME_FORM_TYPE_ID);
		Assert.assertEquals("FormType - Income", t.getName());
		Assert.assertEquals(Constants.INCOME_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.INCOME, t.getTaxType());

        t = formTypeDao.getType(Constants.VAT_FORM_TYPE_ID);
		Assert.assertEquals("FormType - VAT", t.getName());
		Assert.assertEquals(Constants.VAT_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.VAT, t.getTaxType());

        t = formTypeDao.getType(Constants.PROPERTY_FORM_TYPE_ID);
		Assert.assertEquals("FormType - Property", t.getName());
		Assert.assertEquals(Constants.PROPERTY_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.PROPERTY, t.getTaxType());
    }
	
	@Test(expected=DaoException.class)
	public void testWrongIdGet() {
		formTypeDao.getType(-1000);
	}
}
