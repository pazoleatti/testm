package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTypeTemplate;
import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationTemplateListUiHandlers extends UiHandlers {
    void onCreateClicked();
    void onDeleteClicked();
    void onSelectionChanged(DeclarationTypeTemplate selectedItem);
}
