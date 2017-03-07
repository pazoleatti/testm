package com.aplana.sbrf.taxaccounting.web.widget.logarea.server;

import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.GetLogEntriesAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.GetLogEntriesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN', 'N_ROLE_CONF', 'F_ROLE_CONF', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetLogEntriesHandler extends AbstractActionHandler<GetLogEntriesAction, GetLogEntriesResult> {

    @Autowired
    private LogEntryService logEntryService;

    public GetLogEntriesHandler() {
        super(GetLogEntriesAction.class);
    }

    @Override
    public GetLogEntriesResult execute(GetLogEntriesAction action, ExecutionContext context) throws ActionException {
        GetLogEntriesResult result = new GetLogEntriesResult();

        if (action == null || action.getUuid() == null) {
            return result;
        }

        result.setLogEntries(logEntryService.get(action.getUuid(), action.getStart(), action.getLength()));
        result.setLogEntriesCount(logEntryService.getLogCount(action.getUuid()));

        return result;
    }

    @Override
    public void undo(GetLogEntriesAction action, GetLogEntriesResult result, ExecutionContext context) throws ActionException {
        // Не требуется
    }
}