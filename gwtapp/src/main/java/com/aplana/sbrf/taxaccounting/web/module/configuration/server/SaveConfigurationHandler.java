package com.aplana.sbrf.taxaccounting.web.module.configuration.server;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.ConfigTuple;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.SaveConfigurationAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.SaveConfigurationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Component
public class SaveConfigurationHandler extends
		AbstractActionHandler<SaveConfigurationAction, SaveConfigurationResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ConfigurationService configurationService;

	public SaveConfigurationHandler() {
		super(SaveConfigurationAction.class);
	}

	@Override
	public SaveConfigurationResult execute(SaveConfigurationAction action,
			ExecutionContext context) throws ActionException {
		Map<ConfigurationParam, String> map = new HashMap<ConfigurationParam, String>();
		for (ConfigTuple tuple : action.getData()) {
			map.put(tuple.getParam(), tuple.getValue());
		}
		Logger logger = new Logger();
		configurationService.saveAllConfig(securityService.currentUserInfo(), logger, map);
		return new SaveConfigurationResult();
	}

	@Override
	public void undo(SaveConfigurationAction arg0,
			SaveConfigurationResult arg1, ExecutionContext arg2)
			throws ActionException {
		// Ничего не делаем

	}
}
