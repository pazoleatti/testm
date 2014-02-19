package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class PrintAuditDataAction extends UnsecuredActionImpl<PrintAuditDataResult> implements ActionName {

    private LogSystemAuditFilter logSystemFilter;

    public LogSystemAuditFilter getLogSystemFilter() {
        return logSystemFilter;
    }

    public void setLogSystemFilter(LogSystemAuditFilter logSystemFilter) {
        this.logSystemFilter = logSystemFilter;
    }

    @Override
    public String getName() {
        return "";
    }
}
