package com.aplana.sbrf.taxaccounting.web.module.commonparameter.server;

import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.GetCommonParameterAction;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.GetCommonParameterResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class GetCommonParameterHandler extends AbstractActionHandler<GetCommonParameterAction, GetCommonParameterResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ConfigurationService configurationService;

	public GetCommonParameterHandler() {
		super(GetCommonParameterAction.class);
	}

	@Override
	public GetCommonParameterResult execute(GetCommonParameterAction action, ExecutionContext context) throws ActionException {
        GetCommonParameterResult result = new GetCommonParameterResult();
		result.setModel(configurationService.getAllConfig(securityService.currentUserInfo()));
        return result;
    }

	@Override
	public void undo(GetCommonParameterAction arg0, GetCommonParameterResult arg1, ExecutionContext arg2) throws ActionException {
		// Ничего не делаем
	}
}
