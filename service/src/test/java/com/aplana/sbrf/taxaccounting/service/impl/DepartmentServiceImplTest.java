package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamTransport;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DepartmentServiceImplTest {
	
	private static DepartmentService departmentService;
	
	private final static int DEPARTMENT_TB1_ID = 1;
	
	@Before
	public void init(){
		departmentService = new DepartmentServiceImpl();
	}
	
	@Test
	public void testDepDao(){
		Department department = new Department();
		DepartmentDao departmentDao = mock(DepartmentDao.class);
		List<Department> listDep = new ArrayList<Department>();
		listDep.add(new Department());
		listDep.add(new Department());
		listDep.add(new Department());
		ReflectionTestUtils.setField(departmentService, "departmentDao", departmentDao);
		when(departmentDao.getChildren(DEPARTMENT_TB1_ID)).thenReturn(listDep);
		when(departmentDao.getDepartment(DEPARTMENT_TB1_ID)).thenReturn(department);
		when(departmentDao.getIsolatedDepartments()).thenReturn(listDep);
		Assert.assertEquals(3, departmentService.getChildren(DEPARTMENT_TB1_ID).size());
		Assert.assertEquals(department, departmentService.getDepartment(DEPARTMENT_TB1_ID));
		Assert.assertEquals(3, departmentService.getIsolatedDepartments().size());
	}
	
	@Test
	public void testDepParamDao(){
		DepartmentParam departmentParam = new DepartmentParam();
		DepartmentParamIncome departmentParamIncome = new DepartmentParamIncome();
		DepartmentParamTransport departmentParamTransport = new DepartmentParamTransport();
		DepartmentParamDao departmentParamDao = mock(DepartmentParamDao.class);
		ReflectionTestUtils.setField(departmentService, "departmentParamDao", departmentParamDao);
		when(departmentParamDao.getDepartmentParam(DEPARTMENT_TB1_ID)).thenReturn(departmentParam);
		when(departmentParamDao.getDepartmentParamIncome(DEPARTMENT_TB1_ID)).thenReturn(departmentParamIncome);
		when(departmentParamDao.getDepartmentParamTransport(DEPARTMENT_TB1_ID)).thenReturn(departmentParamTransport);
		Assert.assertEquals(departmentParam, departmentService.getDepartmentParam(DEPARTMENT_TB1_ID));
		Assert.assertEquals(departmentParamIncome, departmentService.getDepartmentParamIncome(DEPARTMENT_TB1_ID));
		Assert.assertEquals(departmentParamTransport, departmentService.getDepartmentParamTransport(DEPARTMENT_TB1_ID));
	}

}
