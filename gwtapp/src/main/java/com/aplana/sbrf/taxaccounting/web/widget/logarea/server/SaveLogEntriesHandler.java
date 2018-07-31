package com.aplana.sbrf.taxaccounting.web.widget.logarea.server;

import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_CONF', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
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


            List<LogEntry> logEntries = new ArrayList<LogEntry>();

            for (GWTLogEntry gwtLogEntry:
                    action.getLogEntries()) {
                LogEntry logEntry = new LogEntry();
                logEntry.setLogId(gwtLogEntry.getLogId());
                logEntry.setLevel(gwtLogEntry.getLevel());
                logEntry.setMessage(gwtLogEntry.getMessage());
                logEntry.setObject(gwtLogEntry.getObject());
                logEntry.setOrd(gwtLogEntry.getOrd());
                logEntry.setType(gwtLogEntry.getType());
                logEntry.setDate(gwtLogEntry.getDate());

                logEntries.add(logEntry);
            }


            uuid = logEntryService.save(logEntries);
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