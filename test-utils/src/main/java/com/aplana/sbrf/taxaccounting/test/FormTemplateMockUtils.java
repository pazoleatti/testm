package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;

public final class FormTemplateMockUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private FormTemplateMockUtils() {
	}

	public static FormTemplate mockFormTemplate(int formTemplateId, FormType formType, VersionedObjectStatus status) {
		FormTemplate ft = mock(FormTemplate.class);
		when(ft.getId()).thenReturn(formTemplateId);
		when(ft.getType()).thenReturn(formType);
        when(ft.getStatus()).thenReturn(status);
		return ft;
	}
	
	public static FormTemplate mockFormTemplate(int formTemplateId, int formTypeId, TaxType taxType,
                                                String formTypeName, VersionedObjectStatus status) {
        FormType formType = FormTypeMockUtils.mockFormType(formTypeId, taxType, formTypeName);
        return mockFormTemplate(formTemplateId, formType, status);
    }
}
