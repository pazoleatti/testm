package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

public class FormDataMockUtils {
	public static FormData mockFormData(long id, int departmentId, WorkflowState state) {
		FormData formData = mock(FormData.class);
		when(formData.getId()).thenReturn(id);
		when(formData.getDepartmentId()).thenReturn(departmentId);
		when(formData.getState()).thenReturn(state);
		return formData;
	}
}
