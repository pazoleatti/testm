package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;
import java.util.Map;

public class CheckAccessAction extends UnsecuredActionImpl<CheckAccessResult> implements ActionName {

    private ConfigurationParamModel model;
    private ConfigurationParamGroup group;
    private List<Map<String, String>> emailParams;
    private List<Map<String, String>> asyncParams;

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

    public void setEmailParams(List<Map<String, String>> emailParams) {
        this.emailParams = emailParams;
    }

    public List<Map<String, String>> getEmailParams() {
        return emailParams;
    }

    public void setAsyncParams(List<Map<String, String>> asyncParams) {
        this.asyncParams = asyncParams;
    }

    public List<Map<String, String>> getAsyncParams() {
        return asyncParams;
    }
}
