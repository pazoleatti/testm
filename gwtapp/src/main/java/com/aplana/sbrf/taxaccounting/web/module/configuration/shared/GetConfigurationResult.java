package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class GetConfigurationResult implements Result {
	
	private ConfigurationParamModel model;
    private Map<Integer, String> dereferenceDepartmentNameMap;

    public ConfigurationParamModel getModel() {
        return model;
    }

    public void setModel(ConfigurationParamModel model) {
        this.model = model;
    }

    public Map<Integer, String> getDereferenceDepartmentNameMap() {
        return dereferenceDepartmentNameMap;
    }

    public void setDereferenceDepartmentNameMap(Map<Integer, String> dereferenceDepartmentNameMap) {
        this.dereferenceDepartmentNameMap = dereferenceDepartmentNameMap;
    }
}