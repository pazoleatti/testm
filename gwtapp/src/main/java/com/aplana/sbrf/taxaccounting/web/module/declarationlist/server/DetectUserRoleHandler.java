package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.DetectUserRoleAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.DetectUserRoleResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class DetectUserRoleHandler extends AbstractActionHandler<DetectUserRoleAction, DetectUserRoleResult> {

	@Autowired
	private SecurityService securityService;

	public DetectUserRoleHandler() {
		super(DetectUserRoleAction.class);
	}

	@Override
	public DetectUserRoleResult execute(DetectUserRoleAction action, ExecutionContext executionContext) throws ActionException {
		DetectUserRoleResult result = new DetectUserRoleResult();
		result.setControl(securityService.currentUserInfo().getUser().hasRoles(action.getTaxType(), TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_NS));
		return result;
	}

	@Override
	public void undo(DetectUserRoleAction detectUserRoleAction, DetectUserRoleResult detectUserRoleResult, ExecutionContext executionContext) throws ActionException {
		//DO NOTHING
	}
}
