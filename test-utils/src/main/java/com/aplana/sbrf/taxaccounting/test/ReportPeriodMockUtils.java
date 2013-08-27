package com.aplana.sbrf.taxaccounting.test;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReportPeriodMockUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private ReportPeriodMockUtils() {
	}

	public static ReportPeriod mockReportPeriod(int id) {
		ReportPeriod reportPeriod = mock(ReportPeriod.class);
		when(reportPeriod.getId()).thenReturn(id);
		return reportPeriod;
	}

}
