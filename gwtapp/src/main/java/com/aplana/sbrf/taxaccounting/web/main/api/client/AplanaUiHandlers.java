package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * @author Fail Mukhametdinov
 */
public interface AplanaUiHandlers extends UiHandlers {
    void onRangeChange(final int start, int length);
}
