package com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Map;

public class SaveCommonParameterAction extends UnsecuredActionImpl<SaveCommonParameterResult> implements ActionName {

    private Map<ConfigurationParam, String> configurationParamMap;

    @Override
    public String getName() {
        return "Сохранение общих параметров";
    }

    public Map<ConfigurationParam, String> getConfigurationParamMap() {
        return configurationParamMap;
    }

    public void setConfigurationParamMap(Map<ConfigurationParam, String> configurationParamMap) {
        this.configurationParamMap = configurationParamMap;
    }
}
