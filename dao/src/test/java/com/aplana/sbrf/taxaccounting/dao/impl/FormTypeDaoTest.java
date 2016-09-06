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

import static org.junit.Assert.assertEquals;

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
		assertEquals("FormType - Transport", t.getName());
		assertEquals(DaoTestConstants.TRANSPORT_FORM_TYPE_ID, t.getId());
        assertEquals(TaxType.TRANSPORT, t.getTaxType());
        assertEquals("code_1", t.getCode());
		
		t = formTypeDao.get(DaoTestConstants.INCOME_FORM_TYPE_ID);
		assertEquals("FormType - Income", t.getName());
		assertEquals(DaoTestConstants.INCOME_FORM_TYPE_ID, t.getId());
		assertEquals(TaxType.INCOME, t.getTaxType());
        assertEquals("code_2", t.getCode());

        t = formTypeDao.get(DaoTestConstants.VAT_FORM_TYPE_ID);
		assertEquals("FormType - VAT", t.getName());
		assertEquals(DaoTestConstants.VAT_FORM_TYPE_ID, t.getId());
		assertEquals(TaxType.VAT, t.getTaxType());
        assertEquals("code_3", t.getCode());

        t = formTypeDao.get(DaoTestConstants.PROPERTY_FORM_TYPE_ID);
		assertEquals("FormType - Property", t.getName());
		assertEquals(DaoTestConstants.PROPERTY_FORM_TYPE_ID, t.getId());
		assertEquals(TaxType.PROPERTY, t.getTaxType());
        assertEquals("code_4", t.getCode());
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
        assertEquals(10000, formTypeDao.save(type));
    }

    @Test(expected = DaoException.class)
    public void testSDelete(){
        FormType type = formTypeDao.get(1);
        formTypeDao.delete(type.getId());
        formTypeDao.get(1);
    }

    @Test
    public void testGetByTaxType(){
        assertEquals(1, formTypeDao.getByTaxType(TaxType.TRANSPORT).size());
    }

    @Test
    public void testGetByFilter(){
        TemplateFilter filter = new TemplateFilter();
        filter.setTaxType(TaxType.INCOME);
        filter.setSearchText("IncOmE");
        assertEquals(1, formTypeDao.getByFilter(filter).size());
    }

    @Test
    public void testGetAll() {
        assertEquals(4, formTypeDao.getAll().size());
    }

    @Test
    public void getByCodeTest() {
        Assert.assertNull(formTypeDao.getByCode(null));
        assertEquals(1, formTypeDao.getByCode("code_1").getId());
        assertEquals(2, formTypeDao.getByCode("code_2").getId());
        assertEquals(3, formTypeDao.getByCode("code_3").getId());
        assertEquals(4, formTypeDao.getByCode("codE_4").getId());
    }

    @Test
    public void updateFormType() {
        formTypeDao.updateFormType(1, "testFormName", "testCode", false, null);

        FormType type = formTypeDao.get(1);
        assertEquals("testFormName", type.getName());
        assertEquals("testCode", type.getCode());
    }
}
