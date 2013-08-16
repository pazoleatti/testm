package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetFileUpload extends UnsecuredActionImpl<GetFileUploadResult> implements ActionName {

    private String uuid;

    private Long formDataId;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(Long formDataId) {
        this.formDataId = formDataId;
    }

    @Override
    public String getName() {
        return "Загрузка xls шаблона.";
    }
}
