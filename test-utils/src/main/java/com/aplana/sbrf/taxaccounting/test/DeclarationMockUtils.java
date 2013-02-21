package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.Declaration;

public class DeclarationMockUtils {
	public static Declaration mockDeclaration(long id, int departmentId, boolean accepted) {
		Declaration d = mock(Declaration.class);
		when(d.getId()).thenReturn(id);
		when(d.getDepartmentId()).thenReturn(departmentId);
		when(d.isAccepted()).thenReturn(accepted);
		return d;
	}
	public static Declaration mockDeclaration(long id, int departmentId, boolean accepted, int declarationTemplateId) {
		Declaration d = mockDeclaration(id, departmentId, accepted);
		when(d.getDeclarationTemplateId()).thenReturn(declarationTemplateId);
		return d;
	}
}
