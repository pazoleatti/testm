package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 * Date: 2013
 */
public class GetFTVersionListAction extends UnsecuredActionImpl<GetFTVersionListResult> implements ActionName {
    private int formTemplateId;

    public int getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(int formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

    @Override
    public String getName() {
        return "Получение версий шаблонов НФ";
    }
}
