package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.GetUserRoleUploadTransportDataAction;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.GetUserRoleUploadTransportDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS', 'N_ROLE_ADMIN')")
public class GetUserRoleUploadTransportDataHandler extends AbstractActionHandler<GetUserRoleUploadTransportDataAction, GetUserRoleUploadTransportDataResult> {

	@Autowired
	private SecurityService securityService;

	public GetUserRoleUploadTransportDataHandler() {
		super(GetUserRoleUploadTransportDataAction.class);
	}

	@Override
	public GetUserRoleUploadTransportDataResult execute(GetUserRoleUploadTransportDataAction action, ExecutionContext executionContext) throws ActionException {
		GetUserRoleUploadTransportDataResult result = new GetUserRoleUploadTransportDataResult();
		TAUser taUser = securityService.currentUserInfo().getUser();
		result.setCanUpload(taUser.hasRoles(TARole.N_ROLE_OPER, TARole.F_ROLE_OPER, TARole.N_ROLE_CONTROL_NS,
				TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP,
				TARole.N_ROLE_ADMIN));
		result.setCanLoad(taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP,
				TARole.N_ROLE_ADMIN));
		return result;
	}

	@Override
	public void undo(GetUserRoleUploadTransportDataAction detectUserRoleAction, GetUserRoleUploadTransportDataResult detectUserRoleResult, ExecutionContext executionContext) throws ActionException {
		//DO NOTHING
	}
}
