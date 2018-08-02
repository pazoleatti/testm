package com.aplana.sbrf.taxaccounting.web.module.commonparameter.server;

import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.GetCommonParameterAction;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.GetCommonParameterResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;


@Component
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
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
		result.setModel(configurationService.getCommonConfig(securityService.currentUserInfo()));
        return result;
    }

	@Override
	public void undo(GetCommonParameterAction arg0, GetCommonParameterResult arg1, ExecutionContext arg2) throws ActionException {
		// Ничего не делаем
	}
}
