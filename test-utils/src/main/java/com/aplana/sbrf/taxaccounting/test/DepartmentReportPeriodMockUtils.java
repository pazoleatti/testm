package com.aplana.sbrf.taxaccounting.test;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import org.joda.time.LocalDateTime;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DepartmentReportPeriodMockUtils {

    private DepartmentReportPeriodMockUtils() {}

	public static DepartmentReportPeriod mockDepartmentReportPeriodData(int id, int departmentId, ReportPeriod reportPeriod, boolean isActive, LocalDateTime correctionDate) {
        DepartmentReportPeriod drp = mock(DepartmentReportPeriod.class);
		when(drp.getId()).thenReturn(Long.valueOf(id));
		when(drp.getDepartmentId()).thenReturn(departmentId);
		when(drp.getReportPeriod()).thenReturn(reportPeriod);
        when(drp.isActive()).thenReturn(isActive);
		when(drp.getCorrectionDate()).thenReturn(correctionDate);
		return drp;
	}

}
