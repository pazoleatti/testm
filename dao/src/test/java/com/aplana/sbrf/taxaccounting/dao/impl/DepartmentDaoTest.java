package com.aplana.sbrf.taxaccounting.dao.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentDaoTest.xml"})
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
		Assert.assertEquals(4, d.getFormTypeIds().size());
		Assert.assertTrue(d.getFormTypeIds().contains(1));
		Assert.assertTrue(d.getFormTypeIds().contains(2));
		Assert.assertTrue(d.getFormTypeIds().contains(3));
		Assert.assertTrue(d.getFormTypeIds().contains(4));
		
		d = departmentDao.getDepartment(2);
		Assert.assertEquals(2, d.getId());
		Assert.assertEquals(DepartmentType.TERBANK, d.getType());
		Assert.assertEquals(new Integer(Department.ROOT_BANK_ID), d.getParentId());
		Assert.assertEquals("ТБ1", d.getName());
		Assert.assertEquals(2, d.getFormTypeIds().size());
		Assert.assertTrue(d.getFormTypeIds().contains(1));
		Assert.assertTrue(d.getFormTypeIds().contains(2));

		d = departmentDao.getDepartment(3);
		Assert.assertEquals(3, d.getId());
		Assert.assertEquals(DepartmentType.TERBANK, d.getType());
		Assert.assertEquals(new Integer(Department.ROOT_BANK_ID), d.getParentId());
		Assert.assertEquals("ТБ2", d.getName());
		Assert.assertEquals(0, d.getFormTypeIds().size());
	}
	
	@Test(expected=DaoException.class)
	public void testGetIncorrectId() {
		departmentDao.getDepartment(-1);
	}
	
	@Test
	public void testGetAll() {
		departmentDao.listDepartments();
	}
}
