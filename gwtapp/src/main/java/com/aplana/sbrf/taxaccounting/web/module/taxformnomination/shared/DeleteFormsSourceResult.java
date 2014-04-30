package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.gwtplatform.dispatch.shared.Result;

/**
 * @author auldanov
 */
public class DeleteFormsSourceResult implements Result {
    private String uuid;
    private boolean existFormData;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isExistFormData() {
        return existFormData;
    }

    public void setExistFormData(boolean existFormData) {
        this.existFormData = existFormData;
    }
}
