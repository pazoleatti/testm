package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

public class FormTemplateMockUtils {
	public static FormTemplate mockFormTemplate(int formTemplateId, FormType formType) {
		FormTemplate ft = mock(FormTemplate.class);
		when(ft.getId()).thenReturn(formTemplateId);
		when(ft.getType()).thenReturn(formType);
		return ft;
	}
	
	public static FormTemplate mockFormTemplate(int formTemplateId, int formTypeId, TaxType taxType, String formTypeName) {
		FormType formType = FormTypeMockUtils.mockFormType(formTypeId, taxType, formTypeName);
		return mockFormTemplate(formTemplateId, formType);
	}
}
