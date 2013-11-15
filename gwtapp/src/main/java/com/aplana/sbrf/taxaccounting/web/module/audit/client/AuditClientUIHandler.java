package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * User: avanteev
 */
public interface AuditClientUIHandler extends UiHandlers {
    void onRangeChange(final int start, int length);
}
