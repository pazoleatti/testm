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
		assertEquals(4, departmentFormTypeDao.get(1).size());
	}
	
	@Test
	public void getByDepAndTaxType(){
		assertEquals(1, departmentFormTypeDao.get(1, TaxType.fromCode('T')).size());
	}

	@Test
	public void getFormSources1(){
		assertEquals(6, departmentFormTypeDao.getFormSources(2).size());
	}
	
	@Test
	public void getFormSources2(){
		assertEquals(5, departmentFormTypeDao.getFormSources(2, TaxType.fromCode('T')).size());
	}
	
	@Test
	public void getFormSources3(){
		assertEquals(5, departmentFormTypeDao.getFormSources(2, 1, FormDataKind.fromId(3)).size());
	}
	
	@Test
	public void getDeclarationSources(){
		assertEquals(2, departmentFormTypeDao.getDeclarationSources(2, 1).size());
	}
	
	@Test
	public void getFormDestinations(){
		assertEquals(2, departmentFormTypeDao.getFormDestinations(1, 1, FormDataKind.fromId(3)).size());
	}
}
