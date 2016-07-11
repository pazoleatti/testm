package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class ResidualSaveResult implements Result {
    private String successUuid;
    private String uploadUuid;

    public String getSuccessUuid() {
        return successUuid;
    }

    public void setSuccessUuid(String successUuid) {
        this.successUuid = successUuid;
    }

    public String getUploadUuid() {
        return uploadUuid;
    }

    public void setUploadUuid(String uploadUuid) {
        this.uploadUuid = uploadUuid;
    }
}
