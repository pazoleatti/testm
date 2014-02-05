package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class SetStatusFormResult implements Result {
    private int status;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
