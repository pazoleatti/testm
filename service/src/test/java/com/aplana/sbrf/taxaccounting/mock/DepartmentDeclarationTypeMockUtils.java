package com.aplana.sbrf.taxaccounting.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;

public final class DepartmentDeclarationTypeMockUtils {

    private DepartmentDeclarationTypeMockUtils() {}

	public static DepartmentDeclarationType mockDepartmentDeclarationType(int departmentId, int declarationTypeId) {
		DepartmentDeclarationType ddt = mock(DepartmentDeclarationType.class);
		when(ddt.getDepartmentId()).thenReturn(departmentId);
		when(ddt.getDeclarationTypeId()).thenReturn(declarationTypeId);
		return ddt;
	}	
}
