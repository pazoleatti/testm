package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * User: avanteev
 */
public interface AuditFilterUIHandlers extends UiHandlers {
    void onSearchButtonClicked();
    void onPrintButtonClicked();
    void onArchiveButtonClicked();
}
