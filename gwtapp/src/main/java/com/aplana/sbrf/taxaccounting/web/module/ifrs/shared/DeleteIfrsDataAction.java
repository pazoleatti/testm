package com.aplana.sbrf.taxaccounting.web.module.ifrs.shared;

import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Set;

/**
 * @author Fail Mukhametdinov
 */
public class DeleteIfrsDataAction extends UnsecuredActionImpl<DeleteIfrsDataResult> {
    private Set<IfrsRow> reportPeriodIds;

    public Set<IfrsRow> getReportPeriodIds() {
        return reportPeriodIds;
    }

    public void setReportPeriodIds(Set<IfrsRow> reportPeriodIds) {
        this.reportPeriodIds = reportPeriodIds;
    }
}
