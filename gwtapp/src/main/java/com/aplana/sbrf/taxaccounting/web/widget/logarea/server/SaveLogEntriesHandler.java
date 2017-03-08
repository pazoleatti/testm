package com.aplana.sbrf.taxaccounting.web.widget.logarea.server;

import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN', 'N_ROLE_CONF', 'F_ROLE_CONF', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class SaveLogEntriesHandler extends AbstractActionHandler<SaveLogEntriesAction, SaveLogEntriesResult> {

    @Autowired
    private LogEntryService logEntryService;

    public SaveLogEntriesHandler() {
        super(SaveLogEntriesAction.class);
    }

    @Override
    public SaveLogEntriesResult execute(SaveLogEntriesAction action, ExecutionContext context) throws ActionException {
        String uuid = null;
        if (action != null && action.getLogEntries() != null && !action.getLogEntries().isEmpty()) {
            uuid = logEntryService.save(action.getLogEntries());
        }
        SaveLogEntriesResult result = new SaveLogEntriesResult();
        result.setUuid(uuid);
        return result;
    }

    @Override
    public void undo(SaveLogEntriesAction action, SaveLogEntriesResult result, ExecutionContext context) throws ActionException {
        // Не требуется
    }
}