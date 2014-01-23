package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * User: avanteev
 * Date: 2013
 */
public interface FTVersionListUiHandlers extends UiHandlers {
    void onCreateVersion();
    void onDeleteVersion();
    void onHistoryClick();
    void onReturnClicked();
}
