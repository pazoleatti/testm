package com.aplana.sbrf.taxaccounting.web.module.configuration.server;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.GetConfigurationAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.GetConfigurationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GetConfigurationHandler extends
		AbstractActionHandler<GetConfigurationAction, GetConfigurationResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ConfigurationService configurationService;

    @Autowired
    private RefBookFactory refBookFactory;

	public GetConfigurationHandler() {
		super(GetConfigurationAction.class);
	}

	@Override
	public GetConfigurationResult execute(GetConfigurationAction action,
                                          ExecutionContext context) throws ActionException {

        GetConfigurationResult result = new GetConfigurationResult();
        // Параметры
        result.setModel(configurationService.getAllConfig(securityService.currentUserInfo()));
        result.setEmailConfigs(configurationService.getEmailConfig());
        result.setAsyncConfigs(configurationService.getAsyncConfig());
        // Разыменование подразделений
        Set<Integer> needIds = new HashSet<Integer>();
        ConfigurationParamModel configurationParamModel = result.getModel();
		for (Map.Entry<ConfigurationParam, Map<Integer, List<String>>> entry : configurationParamModel.entrySet()) {
			if (entry.getValue() != null) {
				needIds.addAll(entry.getValue().keySet());
			}
		}
        Map<Integer, String> dereferenceValue = new HashMap<Integer, String>();
        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.Id.DEPARTMENT.getId());
        for (Integer departmentId : needIds) {
            // Хорошо бы разом грузить все подразделения, сейчас провайдер не поддерживает
            Map<String, RefBookValue> map = provider.getRecordData(departmentId.longValue());
            dereferenceValue.put(departmentId, map.get("NAME").getStringValue());
        }
        result.setDereferenceDepartmentNameMap(dereferenceValue);
        return result;
    }

	@Override
	public void undo(GetConfigurationAction arg0, GetConfigurationResult arg1,
			ExecutionContext arg2) throws ActionException {
		// Ничего не делаем
	}
}
