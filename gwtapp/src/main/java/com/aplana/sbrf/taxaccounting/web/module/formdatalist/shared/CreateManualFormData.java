package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CreateManualFormData extends UnsecuredActionImpl<CreateManualFormDataResult> implements ActionName {

    private Long formDataId;

    public Long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(Long formDataId) {
        this.formDataId = formDataId;
    }

    @Override
    public String getName() {
        return "Создание версии ручного ввода";
    }
}
