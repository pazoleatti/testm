package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * User: avanteev
 */
public interface AuditFilterUIHandlers extends UiHandlers {

    void getReportPeriods(TaxType value);

    void onSearchClicked();
}
