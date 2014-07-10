package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.HashSet;
import java.util.Set;

public class SaveConfigurationAction extends UnsecuredActionImpl<SaveConfigurationResult> implements ActionName {

    private ConfigurationParamModel model;

    private Set<Integer> dublicateDepartmentIdSet = new HashSet<Integer>();
    private Set<String> notSetFieldSet = new HashSet<String>();

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

    public Set<Integer> getDublicateDepartmentIdSet() {
        return dublicateDepartmentIdSet;
    }

    public Set<String> getNotSetFieldSet() {
        return notSetFieldSet;
    }
}
