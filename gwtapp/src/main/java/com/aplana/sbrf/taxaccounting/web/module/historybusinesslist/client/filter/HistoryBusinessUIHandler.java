package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.filter;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * User: avanteev
 */
public interface HistoryBusinessUIHandler extends UiHandlers {
    void getReportPeriods(TaxType taxType);
    void onSearchClicked();
}
