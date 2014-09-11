package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CheckAccessAction extends UnsecuredActionImpl<CheckAccessResult> implements ActionName {

    private ConfigurationParamModel model;
    private ConfigurationParamGroup group;

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

    public ConfigurationParamGroup getGroup() {
        return group;
    }

    public void setGroup(ConfigurationParamGroup group) {
        this.group = group;
    }
}
