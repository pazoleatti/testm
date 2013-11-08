package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;

public final class DepartmentMockUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private DepartmentMockUtils() {
	}

	public static Department mockDepartment(int id, DepartmentType type) {
		Department department = mock(Department.class);
		when(department.getId()).thenReturn(id);
		when(department.getType()).thenReturn(type);
		return department;
	}

	public static Department mockDepartment(int id, Integer parentId, DepartmentType type) {
		Department department = mockDepartment(id, type);
		when(department.getParentId()).thenReturn(parentId);
		return department;
	}

	
}
