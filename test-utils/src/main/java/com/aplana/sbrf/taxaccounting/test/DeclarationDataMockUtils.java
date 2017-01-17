package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.State;

public final class DeclarationDataMockUtils {

    private DeclarationDataMockUtils() {}

	public static DeclarationData mockDeclarationData(long id, int departmentId, State state) {
		DeclarationData d = mock(DeclarationData.class);
		when(d.getId()).thenReturn(id);
		when(d.getDepartmentId()).thenReturn(departmentId);
		when(d.getState()).thenReturn(state);
		return d;
	}
	public static DeclarationData mockDeclarationData(long id, int departmentId, State state, int declarationTemplateId) {
		DeclarationData d = mockDeclarationData(id, departmentId, state);
		when(d.getDeclarationTemplateId()).thenReturn(declarationTemplateId);
		return d;
	}
	public static DeclarationData mockDeclarationData(long id, int departmentId, State state, int declarationTemplateId, int reportPeriodId) {
		DeclarationData d = mockDeclarationData(id, departmentId, state, declarationTemplateId);
		when(d.getReportPeriodId()).thenReturn(reportPeriodId);
		return d;
	}
	public static DeclarationData mockDeclarationData(long id, State state, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod) {
		DeclarationData d = mockDeclarationData(id, departmentReportPeriod.getDepartmentId(), state, declarationTemplateId, departmentReportPeriod.getReportPeriod().getId());
        Integer departmentReportPeriodId = departmentReportPeriod.getId();
        when(d.getDepartmentReportPeriodId()).thenReturn(departmentReportPeriodId);
		return d;
	}
}
