package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 * Проверка существования приемника.
 */
public class DestinationCheckAction extends UnsecuredActionImpl<DestinationCheckResult> {
    private long formDataId;

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }
}
