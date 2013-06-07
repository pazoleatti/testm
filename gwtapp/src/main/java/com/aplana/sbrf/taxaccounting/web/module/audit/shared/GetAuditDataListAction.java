package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.model.LogSystemFilter;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetAuditDataListAction extends UnsecuredActionImpl<GetAuditDataListResult> implements ActionName {

    private LogSystemFilter logSystemFilter;

    public LogSystemFilter getLogSystemFilter() {
        return logSystemFilter;
    }

    public void setLogSystemFilter(LogSystemFilter logSystemFilter) {
        this.logSystemFilter = logSystemFilter;
    }

    @Override
    public String getName() {
        return "Получение списка журнала аудита";
    }
}
