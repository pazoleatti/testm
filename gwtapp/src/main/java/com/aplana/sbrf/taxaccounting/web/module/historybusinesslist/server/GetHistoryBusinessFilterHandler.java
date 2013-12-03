package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.server;

import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessFilterAction;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessFilterResult;
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
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetHistoryBusinessFilterHandler extends AbstractActionHandler<GetHistoryBusinessFilterAction, GetHistoryBusinessFilterResult> {

    @Autowired
    AuditService auditService;

    public GetHistoryBusinessFilterHandler() {
        super(GetHistoryBusinessFilterAction.class);
    }

    @Override
    public GetHistoryBusinessFilterResult execute(GetHistoryBusinessFilterAction action, ExecutionContext context) throws ActionException {
        GetHistoryBusinessFilterResult result = new GetHistoryBusinessFilterResult();
        result.setAvailableValues(auditService.getFilterAvailableValues());
        return result;
    }

    @Override
    public void undo(GetHistoryBusinessFilterAction action, GetHistoryBusinessFilterResult result, ExecutionContext context) throws ActionException {
        //Not implemented
    }
}
