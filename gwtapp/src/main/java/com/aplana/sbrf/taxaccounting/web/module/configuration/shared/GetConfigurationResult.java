package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetConfigurationResult implements Result {

	private ConfigurationParamModel model;
    private Map<Integer, String> dereferenceDepartmentNameMap;
    private List<Map<String, String>> emailConfigs;
    private List<Map<String, String>> asyncConfigs;

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

    public List<Map<String, String>> getEmailConfigs() {
        return emailConfigs;
    }

    public void setEmailConfigs(List<Map<String, String>> emailConfigs) {
        this.emailConfigs = emailConfigs;
    }

    public List<Map<String, String>> getAsyncConfigs() {
        return asyncConfigs;
    }

    public void setAsyncConfigs(List<Map<String, String>> asyncConfigs) {
        this.asyncConfigs = asyncConfigs;
    }
}