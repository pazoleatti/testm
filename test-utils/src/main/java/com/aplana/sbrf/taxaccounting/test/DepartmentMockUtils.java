package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;

public class DepartmentMockUtils {
	public static Department mockDepartment(int id, DepartmentType type) {
		Department department = mock(Department.class);
		when(department.getId()).thenReturn(id);
		when(department.getType()).thenReturn(type);
		return department;
	}
}
