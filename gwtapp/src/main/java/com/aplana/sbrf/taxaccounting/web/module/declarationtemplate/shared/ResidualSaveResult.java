package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class ResidualSaveResult implements Result {
    private String successUuid;

    public String getSuccessUuid() {
        return successUuid;
    }

    public void setSuccessUuid(String successUuid) {
        this.successUuid = successUuid;
    }
}
