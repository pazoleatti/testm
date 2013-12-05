package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 * Экшен для проверки существования формы-приемника.
 */
public class DestinationCheckAction extends UnsecuredActionImpl<DestinationCheckResult> implements ActionName {
   private long formDataId;

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }

    @Override
    public String getName() {
        return "Проверка существования формы источника.";
    }
}
