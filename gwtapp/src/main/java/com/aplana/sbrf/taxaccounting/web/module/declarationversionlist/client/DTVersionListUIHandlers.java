package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * User: avanteev
 */
public interface DTVersionListUIHandlers extends UiHandlers {
    void onReturnClicked();
    void onCreateVersion();
    void onDeleteVersion();
    void onHistoryClick();
}
