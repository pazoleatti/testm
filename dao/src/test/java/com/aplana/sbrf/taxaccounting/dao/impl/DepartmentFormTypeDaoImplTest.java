package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DepartmentFormTypeDaoImplTest.xml" })
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DepartmentFormTypeDaoImplTest {
	
	@Autowired
	DepartmentFormTypeDao departmentFormTypeDao;

	@Test
	public void saveSucsess(){
		departmentFormTypeDao.save(1, 5, 2);
	}
	
	@Test(expected = DaoException.class)
	public void saveError(){
		departmentFormTypeDao.save(1, 1, 2);
		departmentFormTypeDao.save(1, 1, 2);
	}
	
	@Test
	public void getByDep(){
		assertEquals(7, departmentFormTypeDao.get(1).size());
	}
	
	@Test
	public void getByDepAndTaxType(){
		assertEquals(3, departmentFormTypeDao.getByTaxType(1, TaxType.TRANSPORT).size());
	}	
	
	@Test
	public void getFormSources(){
		assertEquals(5, departmentFormTypeDao.getFormSources(2, 1, FormDataKind.fromId(3)).size());
	}
	
	@Test
	public void getDeclarationSources(){
		assertEquals(4, departmentFormTypeDao.getDeclarationSources(2, 1).size());
	}
	
	@Test
	public void getFormDestinations(){
		assertEquals(2, departmentFormTypeDao.getFormDestinations(1, 1, FormDataKind.fromId(3)).size());
	}

	@Test
	public void getDeclarationDestinations1(){
		assertEquals(1, departmentFormTypeDao.getDeclarationDestinations(3, 1, FormDataKind.fromId(3)).size());
	}

	@Test
	public void getDeclarationDestinations2(){
		assertEquals(0, departmentFormTypeDao.getDeclarationDestinations(1, 1, FormDataKind.fromId(3)).size());
	}
	
	@Test
	public void testGetAllSources() {
		assertEquals(5, departmentFormTypeDao.getDepartmentSources(2, TaxType.TRANSPORT).size());
	}

    @Test
    public void testGetFormTypeBySource() {
        ArrayList<FormDataKind> formDataKinds = new ArrayList<FormDataKind>(3);
        formDataKinds.add(FormDataKind.ADDITIONAL);
        formDataKinds.add(FormDataKind.CONSOLIDATED);
        formDataKinds.add(FormDataKind.PRIMARY);
        /*Assert.assertEquals(1, departmentFormTypeDao.getFormTypeBySource(1, TaxType.INCOME, formDataKinds).size());*/
    }

    /**
     * Существование форм назначений
     */
    public void existAssignedForm(){
        assertTrue("В подразделении 2 есть форма с типом 1 и видом 3", departmentFormTypeDao.existAssignedForm(2, 1, FormDataKind.SUMMARY));
        assertFalse("В подразделении 2 есть форма с типом 1 и видом 3", departmentFormTypeDao.existAssignedForm(2, 1, FormDataKind.CONSOLIDATED));
    }
}
