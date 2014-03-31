package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Проверка Необходимых условий для перевода формы в режим ручного ввода
 * @author dloshkarev
 */
public class CheckManualAction extends UnsecuredActionImpl<CheckManualResult> implements ActionName {

    private long formDataId;

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }

    @Override
    public String getName() {
        return "Проверка версии ручного ввода";
    }
}
