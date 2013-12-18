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
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_OPER')")
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