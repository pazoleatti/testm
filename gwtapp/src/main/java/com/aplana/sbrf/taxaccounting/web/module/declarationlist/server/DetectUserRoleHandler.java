package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

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
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class DetectUserRoleHandler extends AbstractActionHandler<DetectUserRoleAction, DetectUserRoleResult> {

	@Autowired
	private SecurityService securityService;

	public DetectUserRoleHandler() {
		super(DetectUserRoleAction.class);
	}

	@Override
	public DetectUserRoleResult execute(DetectUserRoleAction detectUserRoleAction, ExecutionContext executionContext) throws ActionException {
		DetectUserRoleResult result = new DetectUserRoleResult();
		result.setUserRole(securityService.currentUserInfo().getUser().getRoles());
		return result;
	}

	@Override
	public void undo(DetectUserRoleAction detectUserRoleAction, DetectUserRoleResult detectUserRoleResult, ExecutionContext executionContext) throws ActionException {
		//DO NOTHING
	}
}
