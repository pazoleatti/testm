package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;

/**
 * User: avanteev
 */
public interface AuditClientUIHandler extends AplanaUiHandlers {
    void onPrintButtonClicked();
    void onArchiveButtonClicked();
    void onSortingChanged();
    void onEventClick(String uuid);
}