package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;

/**
 * User: avanteev
 */
public interface AuditClientUIHandler extends AplanaUiHandlers {
    void onPrintButtonClicked(boolean force);
    void onArchiveButtonClicked();
    void onSortingChanged();
    void onEventClick(String uuid);
    void onTimerReport(ReportType reportType, boolean isTimer);
    void downloadArchive();
    void downloadCsv();
}