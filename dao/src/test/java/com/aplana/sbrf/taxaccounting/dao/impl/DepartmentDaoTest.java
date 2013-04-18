package com.aplana.sbrf.taxaccounting.dao.impl;

import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentDaoTest.xml"})
@Transactional
public class DepartmentDaoTest {
	
	@Autowired
	private DepartmentDao departmentDao;
	
	@Test
	public void testGet() {
		Department d = departmentDao.getDepartment(Department.ROOT_BANK_ID);
		Assert.assertEquals(Department.ROOT_BANK_ID, d.getId());
		Assert.assertEquals(DepartmentType.ROOT_BANK, d.getType());
		Assert.assertEquals("Банк", d.getName());		
		Assert.assertNull(d.getParentId());
		//Assert.assertEquals(4, d.getFormTypeIds().size());
		//Assert.assertTrue(d.getFormTypeIds().contains(1));
		//Assert.assertTrue(d.getFormTypeIds().contains(2));
		//Assert.assertTrue(d.getFormTypeIds().contains(3));
		//Assert.assertTrue(d.getFormTypeIds().contains(4));
		
		d = departmentDao.getDepartment(2);
		Assert.assertEquals(2, d.getId());
		Assert.assertEquals(DepartmentType.TERBANK, d.getType());
		Assert.assertEquals(new Integer(Department.ROOT_BANK_ID), d.getParentId());
		Assert.assertEquals("ТБ1", d.getName());
		//Assert.assertEquals(2, d.getFormTypeIds().size());
		//Assert.assertTrue(d.getFormTypeIds().contains(1));
		//Assert.assertTrue(d.getFormTypeIds().contains(2));

		d = departmentDao.getDepartment(3);
		Assert.assertEquals(3, d.getId());
		Assert.assertEquals(DepartmentType.TERBANK, d.getType());
		Assert.assertEquals(new Integer(Department.ROOT_BANK_ID), d.getParentId());
		Assert.assertEquals("ТБ2", d.getName());
		//Assert.assertEquals(2, d.getFormTypeIds().size());
		
	}
	
	@Test(expected=DaoException.class)
	public void testGetIncorrectId() {
		departmentDao.getDepartment(-1);
	}
	
	@Test
	public void testGetAll() {
		departmentDao.listDepartments();
		Assert.assertEquals(3, departmentDao.getIsolatedDepartments().size());
	}

	@Test
	public void getParentTest(){
		Department department;
		department = departmentDao.getParent(2);
		Assert.assertEquals(1, department.getId());
		Assert.assertEquals("Банк", department.getName());

		department = departmentDao.getParent(3);
		Assert.assertEquals(1, department.getId());
		Assert.assertEquals("Банк", department.getName());
	}
	
	@Test
	public void getSbrfCode(){
		Department department;
		department = departmentDao.getDepartmentBySbrfCode("23");
		Assert.assertNotNull(department);
		Assert.assertTrue(true);
	}
	
	@Test
	public void getName(){
		Department department;
		department = departmentDao.getDepartmentByName("ТБ2");
		Assert.assertNotNull(department);
		Assert.assertEquals(3, department.getId());
	}
}
