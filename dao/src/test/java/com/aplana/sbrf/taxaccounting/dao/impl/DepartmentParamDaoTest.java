package com.aplana.sbrf.taxaccounting.dao.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentParamDaoTest.xml"})
public class DepartmentParamDaoTest {
	@Autowired
	DepartmentParamDao departmentParamDao;
	
	private static final int DEPARTMENT_ID_FOR_TEST = 1;
	
	@Test
	@Transactional
	public void test(){
		DepartmentParam departmentParam =  departmentParamDao.getDepartmentParam(DEPARTMENT_ID_FOR_TEST);
		Assert.assertEquals(DEPARTMENT_ID_FOR_TEST, departmentParam.getDepartmentId());
	}

}
