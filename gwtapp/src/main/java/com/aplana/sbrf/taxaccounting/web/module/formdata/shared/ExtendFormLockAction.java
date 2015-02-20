package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class ExtendFormLockAction extends UnsecuredActionImpl<ExtendFormLockResult> {
    long formDataId;

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }
}
