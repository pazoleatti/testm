package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;

public class DepartmentDeclarationTypeMockUtils {
	public static DepartmentDeclarationType mockDepartmentDeclarationType(int departmentId, int declarationTypeId) {
		DepartmentDeclarationType ddt = mock(DepartmentDeclarationType.class);
		when(ddt.getDepartmentId()).thenReturn(departmentId);
		when(ddt.getDeclarationTypeId()).thenReturn(declarationTypeId);
		return ddt;
	}	
}
