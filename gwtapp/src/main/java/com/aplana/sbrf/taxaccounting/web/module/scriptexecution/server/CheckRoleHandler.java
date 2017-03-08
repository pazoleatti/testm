package com.aplana.sbrf.taxaccounting.web.module.scriptexecution.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.scriptexecution.shared.CheckRoleAction;
import com.aplana.sbrf.taxaccounting.web.module.scriptexecution.shared.CheckRoleResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class CheckRoleHandler extends AbstractActionHandler<CheckRoleAction, CheckRoleResult> {

    @Autowired
    SecurityService securityService;

    public CheckRoleHandler() {
        super(CheckRoleAction.class);
    }

    @Override
    public CheckRoleResult execute(CheckRoleAction checkRoleAction, ExecutionContext executionContext) throws ActionException {
        TAUser user = securityService.currentUserInfo().getUser();
        CheckRoleResult result = new CheckRoleResult();
        result.setResult(user.hasRoles(TARole.N_ROLE_CONF, TARole.F_ROLE_CONF));
        return result;
    }

    @Override
    public void undo(CheckRoleAction checkRoleAction, CheckRoleResult checkRoleResult, ExecutionContext executionContext) throws ActionException {
    }
}
