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
    private boolean force;

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    @Override
    public String getName() {
        return "";
    }
}
