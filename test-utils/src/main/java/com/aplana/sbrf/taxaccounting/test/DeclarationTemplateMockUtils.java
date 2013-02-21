package com.aplana.sbrf.taxaccounting.test;

import static com.aplana.sbrf.taxaccounting.test.DeclarationTypeMockUtils.mockDeclarationType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;

public class DeclarationTemplateMockUtils {
	public static DeclarationTemplate mockDeclarationTemplate(int id, int declarationTypeId) {
		DeclarationType declarationType = mockDeclarationType(declarationTypeId);
		
		DeclarationTemplate declarationTemplate = mock(DeclarationTemplate.class);
		when(declarationTemplate.getId()).thenReturn(id);
		when(declarationTemplate.getDeclarationType()).thenReturn(declarationType);
		return declarationTemplate;
	}
}
