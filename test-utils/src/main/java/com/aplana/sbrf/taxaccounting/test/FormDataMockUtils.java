package com.aplana.sbrf.taxaccounting.test;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormDataMockUtils {
	public static FormData mockFormData(long id, int departmentId, WorkflowState state, int reportPeriodId) {
		FormData formData = mock(FormData.class);
		when(formData.getId()).thenReturn(id);
		when(formData.getDepartmentId()).thenReturn(departmentId);
		when(formData.getState()).thenReturn(state);
		when(formData.getReportPeriodId()).thenReturn(reportPeriodId);
		return formData;
	}
}
