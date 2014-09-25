package com.aplana.sbrf.taxaccounting.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;

public final class DeclarationDataMockUtils {

    private DeclarationDataMockUtils() {}

	public static DeclarationData mockDeclarationData(long id, int departmentId, boolean accepted) {
		DeclarationData d = mock(DeclarationData.class);
		when(d.getId()).thenReturn(id);
		when(d.getDepartmentId()).thenReturn(departmentId);
		when(d.isAccepted()).thenReturn(accepted);
		return d;
	}
	public static DeclarationData mockDeclarationData(long id, int departmentId, boolean accepted, int declarationTemplateId) {
		DeclarationData d = mockDeclarationData(id, departmentId, accepted);
		when(d.getDeclarationTemplateId()).thenReturn(declarationTemplateId);
		return d;
	}
	public static DeclarationData mockDeclarationData(long id, int departmentId, boolean accepted, int declarationTemplateId, int reportPeriodId) {
		DeclarationData d = mockDeclarationData(id, departmentId, accepted, declarationTemplateId);
		when(d.getReportPeriodId()).thenReturn(reportPeriodId);
		return d;
	}
	public static DeclarationData mockDeclarationData(long id, boolean accepted, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod) {
		DeclarationData d = mockDeclarationData(id, departmentReportPeriod.getDepartmentId(), accepted, declarationTemplateId, departmentReportPeriod.getReportPeriod().getId());
        Integer departmentReportPeriodId = departmentReportPeriod.getId();
        when(d.getDepartmentReportPeriodId()).thenReturn(departmentReportPeriodId);
		return d;
	}
}
