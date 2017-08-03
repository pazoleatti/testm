package com.aplana.sbrf.taxaccounting.web.module.commonparameter.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.GetUserRoleCommonParameterAction;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.GetUserRoleCommonParameterResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetUserCommonParameterHandler extends AbstractActionHandler<GetUserRoleCommonParameterAction, GetUserRoleCommonParameterResult> {

    @Autowired
    private SecurityService securityService;

    public GetUserCommonParameterHandler() {
        super(GetUserRoleCommonParameterAction.class);
    }

    @Override
    public GetUserRoleCommonParameterResult execute(GetUserRoleCommonParameterAction action, ExecutionContext context) throws ActionException {
        GetUserRoleCommonParameterResult result = new GetUserRoleCommonParameterResult();
        TAUser taUser = securityService.currentUserInfo().getUser();
        result.setCanEdit(taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP));
        return result;
    }

    @Override
    public void undo(GetUserRoleCommonParameterAction action, GetUserRoleCommonParameterResult result, ExecutionContext context) throws ActionException {

    }
}
