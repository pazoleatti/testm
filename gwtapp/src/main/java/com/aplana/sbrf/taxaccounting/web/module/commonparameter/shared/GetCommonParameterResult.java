package com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.gwtplatform.dispatch.shared.Result;

public class GetCommonParameterResult implements Result {

	private ConfigurationParamModel model;

    public ConfigurationParamModel getModel() {
        return model;
    }

    public void setModel(ConfigurationParamModel model) {
        this.model = model;
    }
}