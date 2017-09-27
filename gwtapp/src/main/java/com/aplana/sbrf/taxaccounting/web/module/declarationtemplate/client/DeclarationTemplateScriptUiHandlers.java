package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationTemplateScriptUiHandlers extends UiHandlers {
    void onInfoChanged();
    void onSelectScript(String eventTitle);
    void onOpenEventChoiceDialog();
    void onRemoveEventScript(int selectedIndex);
}
