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

//TODO: переработать, чтобы не было необходимости поднимать полный файл dao.xml, а то получается integration-тест вместо unit-теста 
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext.xml", "classpath:com/aplana/sbrf/taxaccounting/dao.xml"})
public class DepartmentDaoTest {
	
	@Autowired
	private DepartmentDao departmentDao;
	
	@Test
	public void testGet() {
		Department d = departmentDao.getDepartment(Department.ROOT_BANK_ID);
		Assert.assertEquals(Department.ROOT_BANK_ID, d.getId());
		Assert.assertEquals(DepartmentType.ROOT_BANK, d.getType());
		Assert.assertNull(d.getParentId());
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
