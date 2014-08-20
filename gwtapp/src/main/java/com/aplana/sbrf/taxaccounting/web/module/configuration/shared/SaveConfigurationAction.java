package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SaveConfigurationAction extends UnsecuredActionImpl<SaveConfigurationResult> implements ActionName {

    private ConfigurationParamModel model;

    private Set<Integer> dublicateDepartmentIdSet = new HashSet<Integer>();
    private Map<Integer, Set<String>> notSetFields = new HashMap<Integer, Set<String>>();

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

    public Map<Integer, Set<String>> getNotSetFields() {
        return notSetFields;
    }
}
