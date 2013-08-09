package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
		Assert.assertEquals(3, departmentService.getChildren(DEPARTMENT_TB1_ID).size());
		Assert.assertEquals(department, departmentService.getDepartment(DEPARTMENT_TB1_ID));
	}
	
	@Test
	public void listAllTest(){
		DepartmentDao departmentDao = mock(DepartmentDao.class);
		ReflectionTestUtils.setField(departmentService, "departmentDao", departmentDao);
		when(departmentDao.listDepartments()).thenReturn(new ArrayList<Department>());

		departmentService.listAll();
		verify(departmentDao, times(1)).listDepartments();
	}

	@Test
	public void getParentTest(){
		DepartmentDao departmentDao = mock(DepartmentDao.class);
		ReflectionTestUtils.setField(departmentService, "departmentDao", departmentDao);
		Integer departmentId = anyInt();
		when(departmentDao.getParent(departmentId)).thenReturn(new Department());

		departmentService.getParent(departmentId);
		verify(departmentDao, times(1)).getParent(departmentId);
	}

	@Test
	public void getRequiredForTreeDepartmentsTest(){
		Set<Integer> available = new HashSet<Integer>(Arrays.asList(2, 3));
		DepartmentDao departmentDao = mock(DepartmentDao.class);
		ReflectionTestUtils.setField(departmentService, "departmentDao", departmentDao);

		Department root = new Department();
		root.setName("Bank");
		Department departmentTB2 = new Department();
		departmentTB2.setName("TB2");
		Department departmentTB3 = new Department();
		departmentTB3.setName("TB3");

		when(departmentDao.getDepartment(2)).thenReturn(departmentTB2);
		when(departmentDao.getDepartment(3)).thenReturn(departmentTB3);
		when(departmentDao.getParent(2)).thenReturn(root);
		when(departmentDao.getParent(3)).thenReturn(root);

		Collection<Department> result = departmentService.getRequiredForTreeDepartments(available).values();
		verify(departmentDao, times(1)).getDepartment(2);
		verify(departmentDao, times(1)).getParent(2);
		verify(departmentDao, times(1)).getDepartment(3);
		verify(departmentDao, times(1)).getParent(3);
		Assert.assertEquals(3, result.size());
		Assert.assertEquals(true, result.contains(root));
		Assert.assertEquals(true, result.contains(departmentTB2));
		Assert.assertEquals(true, result.contains(departmentTB3));
	}

}
