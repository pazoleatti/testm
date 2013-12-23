package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Vitalii Samolovskikh
 */
public class GetFormTemplateListAction extends UnsecuredActionImpl<GetFormTemplateListResult> {
    TemplateFilter filter;

    public TemplateFilter getFilter() {
        return filter;
    }

    public void setFilter(TemplateFilter filter) {
        this.filter = filter;
    }
}
