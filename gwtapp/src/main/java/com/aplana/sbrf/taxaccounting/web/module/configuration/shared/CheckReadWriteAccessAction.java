package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CheckReadWriteAccessAction extends UnsecuredActionImpl<CheckReadWriteAccessResult> implements ActionName {

    private ConfigurationParamModel model;

    @Override
    public String getName() {
        return "Проверка доступа";
    }

    public ConfigurationParamModel getModel() {
        return model;
    }

    public void setModel(ConfigurationParamModel model) {
        this.model = model;
    }
}