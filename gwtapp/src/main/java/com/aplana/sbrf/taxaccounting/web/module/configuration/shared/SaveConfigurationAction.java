package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class SaveConfigurationAction extends UnsecuredActionImpl<SaveConfigurationResult> implements ActionName {

    private ConfigurationParamModel model;

    @Override
    public String getName() {
        return "Сохранение конфигурационных параметров";
    }

    public ConfigurationParamModel getModel() {
        return model;
    }

    public void setModel(ConfigurationParamModel model) {
        this.model = model;
    }
}
