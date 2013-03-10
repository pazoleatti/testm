package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
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
	
	public static Department mockDepartment(int id, Integer parentId, DepartmentType type, List<DepartmentFormType> dfts) {
		Department department = mockDepartment(id, parentId, type);
		when(department.getDepartmentFormTypes()).thenReturn(dfts);
		return department;
	}
	
}
