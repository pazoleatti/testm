package com.aplana.sbrf.taxaccounting.test;

import com.aplana.sbrf.taxaccounting.model.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class FormDataMockUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private FormDataMockUtils() {
	}

	public static FormData mockFormData(long id, int departmentId, WorkflowState state, FormDataKind kind, int reportPeriodId) {
		FormData formData = mock(FormData.class);
		when(formData.getId()).thenReturn(id);
		when(formData.getDepartmentId()).thenReturn(departmentId);
		when(formData.getState()).thenReturn(state);
		when(formData.getKind()).thenReturn(kind);
		when(formData.getReportPeriodId()).thenReturn(reportPeriodId);
		return formData;
	}

	public static FormData mockFormData(long id, int departmentId, WorkflowState state, FormDataKind kind, int reportPeriodId, FormType formType) {
		FormData formData = mock(FormData.class);
		when(formData.getId()).thenReturn(id);
		when(formData.getDepartmentId()).thenReturn(departmentId);
		when(formData.getState()).thenReturn(state);
		when(formData.getKind()).thenReturn(kind);
		when(formData.getReportPeriodId()).thenReturn(reportPeriodId);
		when(formData.getFormType()).thenReturn(formType);
		return formData;
	}

    public static FormData mockFormData(long id, WorkflowState state, FormDataKind kind, FormType formType, DepartmentReportPeriod departmentReportPeriod) {
        FormData formData = mockFormData(id, departmentReportPeriod.getDepartmentId(), state, kind, departmentReportPeriod.getReportPeriod().getId(), formType);
        Integer departmentReportPeriodId = departmentReportPeriod.getId();
        when(formData.getDepartmentReportPeriodId()).thenReturn(departmentReportPeriodId);
        return formData;
    }
}
