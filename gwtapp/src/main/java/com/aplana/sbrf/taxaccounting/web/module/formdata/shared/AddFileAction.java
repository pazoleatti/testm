package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Добавление сфайла в НФ
 * @author Lhaziev
 */
public class AddFileAction extends UnsecuredActionImpl<AddFileResult> {
    private FormData formData;
    private String uuid;

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
