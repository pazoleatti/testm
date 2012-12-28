package com.aplana.sbrf.taxaccounting.test;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReportPeriodMockUtils {
	public static ReportPeriod mockReportPeriod(boolean isActive) {
		ReportPeriod reportPeriod = mock(ReportPeriod.class);
		when(reportPeriod.isActive()).thenReturn(isActive);
		return reportPeriod;
	}
}
