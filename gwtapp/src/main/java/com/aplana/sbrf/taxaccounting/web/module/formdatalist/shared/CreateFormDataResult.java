package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreateFormDataResult implements Result {
    private static final long serialVersionUID = 3481378728453321804L;

    private long formDataId;
    private String uuid;

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
