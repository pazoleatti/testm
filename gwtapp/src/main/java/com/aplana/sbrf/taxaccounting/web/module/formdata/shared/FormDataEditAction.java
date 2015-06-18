package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Проверка Необходимых условий для перевода формы в режим ручного ввода
 * @author dloshkarev
 */
public class FormDataEditAction extends UnsecuredActionImpl<FormDataEditResult> implements ActionName {

    private FormData formData;

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    @Override
    public String getName() {
        return "";
    }
}
