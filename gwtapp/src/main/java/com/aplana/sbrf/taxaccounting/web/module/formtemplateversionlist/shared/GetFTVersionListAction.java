package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 * Date: 2013
 * Кллсс для получнния списка версий макета
 */
public class GetFTVersionListAction extends UnsecuredActionImpl<GetFTVersionListResult> implements ActionName {
    //Идентификатор макета FormType
    private int formTypeId;

    public int getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(int formTypeId) {
        this.formTypeId = formTypeId;
    }

    @Override
    public String getName() {
        return "Получение версий шаблонов НФ";
    }
}
