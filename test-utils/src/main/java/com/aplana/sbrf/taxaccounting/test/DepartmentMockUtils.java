package com.aplana.sbrf.taxaccounting.test;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DepartmentMockUtils {
	public static Department mockDepartment(int id, DepartmentType type) {
		Department department = mock(Department.class);
		when(department.getId()).thenReturn(id);
		when(department.getType()).thenReturn(type);
		return department;
	}

	public static Department mockDepartment(int id, int parentId, DepartmentType type) {
		Department department = mock(Department.class);
		when(department.getId()).thenReturn(id);
		when(department.getType()).thenReturn(type);
		when(department.getParentId()).thenReturn(parentId);
		return department;
	}
}
