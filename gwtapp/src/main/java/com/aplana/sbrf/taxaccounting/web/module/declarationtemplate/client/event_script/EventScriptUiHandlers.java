package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event_script;

import com.gwtplatform.mvp.client.UiHandlers;

public interface EventScriptUiHandlers extends UiHandlers {
    void onCreate(int formDataEventId);
    void onClose();
}
