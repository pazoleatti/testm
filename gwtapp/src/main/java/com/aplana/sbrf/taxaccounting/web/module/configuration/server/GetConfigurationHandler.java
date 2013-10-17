package com.aplana.sbrf.taxaccounting.web.module.configuration.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.ConfigTuple;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.GetConfigurationAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.GetConfigurationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Component
public class GetConfigurationHandler extends
		AbstractActionHandler<GetConfigurationAction, GetConfigurationResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ConfigurationService configurationService;

	public GetConfigurationHandler() {
		super(GetConfigurationAction.class);
	}

	@Override
	public GetConfigurationResult execute(GetConfigurationAction action,
			ExecutionContext context) throws ActionException {
		
		List<ConfigTuple> tuples = new ArrayList<ConfigTuple>();
		for (Map.Entry<ConfigurationParam, String> entry : configurationService.getAllConfig(securityService.currentUserInfo()).entrySet()) {
			ConfigTuple configTuple = new ConfigTuple();
			configTuple.setParam(entry.getKey());
			configTuple.setValue(entry.getValue());
			tuples.add(configTuple);
		}
		GetConfigurationResult result = new GetConfigurationResult();
		result.setData(tuples);
		return result;
	}

	@Override
	public void undo(GetConfigurationAction arg0, GetConfigurationResult arg1,
			ExecutionContext arg2) throws ActionException {
		// Ничего не делаем

	}
}
