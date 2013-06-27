package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class OpenPeriodAction extends UnsecuredActionImpl<OpenPeriodResult> {
	int reportPeriodId;

	public int getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(int reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}
}
