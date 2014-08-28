package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class HasManualVersionAction  extends UnsecuredActionImpl<HasManualVersionResult> {

    long formDataId;

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }
}
