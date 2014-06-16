package com.aplana.sbrf.taxaccounting.web.module.configuration.server;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.ConfigTuple;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.SaveConfigurationAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.SaveConfigurationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        ConfigurationParamModel model = new ConfigurationParamModel();
        for (ConfigTuple tuple : action.getData()) {
            model.setFullStringValue(tuple.getParam(), tuple.getValue());
        }
		configurationService.saveAllConfig(securityService.currentUserInfo(), model);
		return new SaveConfigurationResult();
	}

	@Override
	public void undo(SaveConfigurationAction arg0,
			SaveConfigurationResult arg1, ExecutionContext arg2)
			throws ActionException {
		// Ничего не делаем
	}
}
