package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * User: avanteev
 */
public interface HistoryBusinessUIHandler extends UiHandlers {
    void onRangeChange(final int start, int length);
}
