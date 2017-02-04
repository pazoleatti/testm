package com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class SaveCommonParameterResult implements Result {

    /**
     * Идентификатор лога с ошибкой.
     */
    private String uuid;

    /**
     * Список параметров с ошибками
     */
    private List<ConfigurationParam> errors;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<ConfigurationParam> getErrors() {
        return errors;
    }

    public void setErrors(List<ConfigurationParam> errors) {
        this.errors = errors;
    }
}