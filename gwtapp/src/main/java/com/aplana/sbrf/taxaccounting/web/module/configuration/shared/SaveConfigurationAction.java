package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.*;

public class SaveConfigurationAction extends UnsecuredActionImpl<SaveConfigurationResult> implements ActionName {

    private ConfigurationParamModel model;

    private Set<Integer> dublicateDepartmentIdSet = new HashSet<Integer>();
    private Map<Integer, Set<String>> notSetFields = new HashMap<Integer, Set<String>>();
    private List<Map<String, String>> emailParams;
    private List<Map<String, String>> asyncParams;

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
