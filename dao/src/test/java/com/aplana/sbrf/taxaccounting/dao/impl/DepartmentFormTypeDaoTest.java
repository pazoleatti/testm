package com.aplana.sbrf.taxaccounting.dao.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DepartmentFormTypeDaoTest.xml" })
@Transactional
public class DepartmentFormTypeDaoTest {
	
	@Autowired
	DepartmentFormTypeDao departmentFormTypeDao;
	
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
}
