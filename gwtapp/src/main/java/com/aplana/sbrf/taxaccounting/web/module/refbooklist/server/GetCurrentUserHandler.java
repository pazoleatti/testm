package com.aplana.sbrf.taxaccounting.web.module.refbooklist.server;

import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetCurrentUserAction;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetCurrentUserResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS', 'N_ROLE_CONF', 'F_ROLE_CONF')")
@Component
public class GetCurrentUserHandler extends AbstractActionHandler<GetCurrentUserAction, GetCurrentUserResult> {

    public GetCurrentUserHandler() {
        super(GetCurrentUserAction.class);
    }

    @Autowired
    private SecurityService securityService;

    @Override
    public GetCurrentUserResult execute(GetCurrentUserAction getCurrentUserAction, ExecutionContext executionContext) throws ActionException {
        GetCurrentUserResult result = new GetCurrentUserResult();
        result.setUser(securityService.currentUserInfo().getUser());
        return result;
    }

    @Override
    public void undo(GetCurrentUserAction getCurrentUserAction, GetCurrentUserResult getCurrentUserResult, ExecutionContext executionContext) throws ActionException {

    }
}
