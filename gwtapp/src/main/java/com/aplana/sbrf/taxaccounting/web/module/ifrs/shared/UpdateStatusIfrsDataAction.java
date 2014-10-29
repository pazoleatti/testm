package com.aplana.sbrf.taxaccounting.web.module.ifrs.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * @author lhaziev
 */
public class UpdateStatusIfrsDataAction extends UnsecuredActionImpl<UpdateStatusIrfsDataResult> implements ActionName{

    private List<Integer> reportPeriodIds;

    public List<Integer> getReportPeriodIds() {
        return reportPeriodIds;
    }

    public void setReportPeriodIds(List<Integer> reportPeriodIds) {
        this.reportPeriodIds = reportPeriodIds;
    }

    @Override
	public String getName() {
		return "Получение статусов отчетностей для МСФО";
	}
}
