package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormTypeDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FormTypeDaoTest {
	@Autowired
	private FormTypeDao formTypeDao;

    @Test
	public void testGet() {
		FormType t = formTypeDao.get(DaoTestConstants.TRANSPORT_FORM_TYPE_ID);
		Assert.assertEquals("FormType - Transport", t.getName());
		Assert.assertEquals(DaoTestConstants.TRANSPORT_FORM_TYPE_ID, t.getId());
        Assert.assertEquals(TaxType.TRANSPORT, t.getTaxType());
        Assert.assertEquals("code_1", t.getCode());
		
		t = formTypeDao.get(DaoTestConstants.INCOME_FORM_TYPE_ID);
		Assert.assertEquals("FormType - Income", t.getName());
		Assert.assertEquals(DaoTestConstants.INCOME_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.INCOME, t.getTaxType());
        Assert.assertEquals("code_2", t.getCode());

        t = formTypeDao.get(DaoTestConstants.VAT_FORM_TYPE_ID);
		Assert.assertEquals("FormType - VAT", t.getName());
		Assert.assertEquals(DaoTestConstants.VAT_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.VAT, t.getTaxType());
        Assert.assertEquals("code_3", t.getCode());

        t = formTypeDao.get(DaoTestConstants.PROPERTY_FORM_TYPE_ID);
		Assert.assertEquals("FormType - Property", t.getName());
		Assert.assertEquals(DaoTestConstants.PROPERTY_FORM_TYPE_ID, t.getId());
		Assert.assertEquals(TaxType.PROPERTY, t.getTaxType());
        Assert.assertEquals("code_4", t.getCode());
    }
	
	@Test(expected=DaoException.class)
	public void testWrongIdGet() {
		formTypeDao.get(-1000);
	}

    @Test
    public void testSave(){
        FormType type = new FormType();
        type.setName("");
        type.setTaxType(TaxType.DEAL);
        type.setStatus(VersionedObjectStatus.DRAFT);
		type.setCode("code_5");
        Assert.assertEquals(10000, formTypeDao.save(type));
    }

    @Test(expected = DaoException.class)
    public void testSDelete(){
        FormType type = formTypeDao.get(1);
        formTypeDao.delete(type.getId());
        formTypeDao.get(1);
    }

    @Test
    public void testGetByTaxType(){
        Assert.assertEquals(1, formTypeDao.getByTaxType(TaxType.TRANSPORT).size());
    }

    @Test
    public void testGetByFilter(){
        TemplateFilter filter = new TemplateFilter();
        filter.setTaxType(TaxType.INCOME);
        filter.setSearchText("IncOmE");
        Assert.assertEquals(1, formTypeDao.getByFilter(filter).size());
    }

    @Test
    public void testGetAll() {
        Assert.assertEquals(4, formTypeDao.getAll().size());
    }

    @Test
    public void getByCodeTest() {
        Assert.assertNull(formTypeDao.getByCode(null));
        Assert.assertEquals(1, formTypeDao.getByCode("code_1").getId());
        Assert.assertEquals(2, formTypeDao.getByCode("code_2").getId());
        Assert.assertEquals(3, formTypeDao.getByCode("code_3").getId());
        Assert.assertEquals(4, formTypeDao.getByCode("codE_4").getId());
    }
}
