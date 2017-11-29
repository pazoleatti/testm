package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateCheck;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface DeclarationTemplateChecksUiHandlers extends UiHandlers {
    List<DeclarationTemplateCheck> getChecks();
}
