package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * @author Fail Mukhametdinov
 */
public interface RefBookScriptUiHandlers extends UiHandlers {
    void saveScript(boolean isRedirect);

    void cancelEdit();

    void getScript();

    void onHistoryClicked();
}
