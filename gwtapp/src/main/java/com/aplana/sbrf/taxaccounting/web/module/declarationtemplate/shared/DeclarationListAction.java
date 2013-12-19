package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class DeclarationListAction extends UnsecuredActionImpl<DeclarationListResult> {
    TemplateFilter filter;

    public TemplateFilter getFilter() {
        return filter;
    }

    public void setFilter(TemplateFilter filter) {
        this.filter = filter;
    }
}
