package com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared;

import com.gwtplatform.dispatch.shared.Result;

public class SaveCommonParameterResult implements Result {
    /**
     * Идентификатор лога с ошибкой.
     */
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}