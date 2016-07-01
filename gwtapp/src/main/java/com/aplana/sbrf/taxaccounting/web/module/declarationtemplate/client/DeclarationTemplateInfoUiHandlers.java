package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationTemplateInfoUiHandlers extends UiHandlers {
    void onDeleteXsd();
    void onCheckBeforeDeleteJrxml();
    void downloadJrxml();
    void downloadXsd();
    void onInfoChanged();
}
