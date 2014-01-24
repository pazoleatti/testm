package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
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
		FormType t = formTypeDao.get(Constants.TRANSPORT_FORM_TYPE_ID);
		Assert.assertEquals("FormType - Transport", t.getName());
		Assert.assertEquals(Constants.TRANSPORT_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.TRANSPORT, t.getTaxType());
		
		t = formTypeDao.get(Constants.INCOME_FORM_TYPE_ID);
		Assert.assertEquals("FormType - Income", t.getName());
		Assert.assertEquals(Constants.INCOME_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.INCOME, t.getTaxType());

        t = formTypeDao.get(Constants.VAT_FORM_TYPE_ID);
		Assert.assertEquals("FormType - VAT", t.getName());
		Assert.assertEquals(Constants.VAT_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.VAT, t.getTaxType());

        t = formTypeDao.get(Constants.PROPERTY_FORM_TYPE_ID);
		Assert.assertEquals("FormType - Property", t.getName());
		Assert.assertEquals(Constants.PROPERTY_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.PROPERTY, t.getTaxType());
    }
	
	@Test(expected=DaoException.class)
	public void testWrongIdGet() {
		formTypeDao.get(-1000);
	}

    @Test
    public void testSave(){
        FormType type = formTypeDao.get(1);
        type.setStatus(VersionedObjectStatus.DRAFT);
        Assert.assertEquals(10000, formTypeDao.save(type));
    }

    @Test
    public void testSDelete(){
        FormType type = formTypeDao.get(1);
        formTypeDao.delete(type.getId());
        type = formTypeDao.get(1);
        Assert.assertEquals(-1, type.getStatus().getId());
    }

    @Test
    public void testGetByFilter(){
        TemplateFilter filter = new TemplateFilter();
        filter.setTaxType(TaxType.INCOME);
        filter.setActive(true);
        Assert.assertEquals(1, formTypeDao.getByFilter(filter).size());
        filter.setActive(false);
        Assert.assertEquals(0, formTypeDao.getByFilter(filter).size());
    }
}
