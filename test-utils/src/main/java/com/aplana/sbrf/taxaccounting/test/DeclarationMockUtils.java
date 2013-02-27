package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;

public class DeclarationMockUtils {
	public static DeclarationData mockDeclaration(long id, int departmentId, boolean accepted) {
		DeclarationData d = mock(DeclarationData.class);
		when(d.getId()).thenReturn(id);
		when(d.getDepartmentId()).thenReturn(departmentId);
		when(d.isAccepted()).thenReturn(accepted);
		return d;
	}
	public static DeclarationData mockDeclaration(long id, int departmentId, boolean accepted, int declarationTemplateId) {
		DeclarationData d = mockDeclaration(id, departmentId, accepted);
		when(d.getDeclarationTemplateId()).thenReturn(declarationTemplateId);
		return d;
	}
}
