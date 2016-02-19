package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationTemplateColumnUiHandlers extends UiHandlers {
    void addSubreport(DeclarationSubreport subreport);

    void removeSubreport(DeclarationSubreport subreport);

    void flushSubreport(DeclarationSubreport subreport);
}
