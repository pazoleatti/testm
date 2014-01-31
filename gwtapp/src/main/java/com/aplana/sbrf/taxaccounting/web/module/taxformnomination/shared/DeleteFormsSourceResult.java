package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.gwtplatform.dispatch.shared.Result;

/**
 * @author auldanov
 */
public class DeleteFormsSourceResult implements Result {
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
