package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.SetArchiveVisibleAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.SetArchiveVisibleResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class SetArchiveVisibleHandler extends AbstractActionHandler<SetArchiveVisibleAction, SetArchiveVisibleResult> {

    @Autowired
    private SecurityService securityService;

    public SetArchiveVisibleHandler() {
        super(SetArchiveVisibleAction.class);
    }

    @Override
    public SetArchiveVisibleResult execute(SetArchiveVisibleAction action, ExecutionContext executionContext) throws ActionException {
        SetArchiveVisibleResult result = new SetArchiveVisibleResult();
        result.setVisible(securityService.currentUserInfo().getUser().hasRole(TARole.N_ROLE_ADMIN));
        return result;
    }

    @Override
    public void undo(SetArchiveVisibleAction setArchiveVisibleAction, SetArchiveVisibleResult setArchiveVisibleResult, ExecutionContext executionContext) throws ActionException {

    }
}
