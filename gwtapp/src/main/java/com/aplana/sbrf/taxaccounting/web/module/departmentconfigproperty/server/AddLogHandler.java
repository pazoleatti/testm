package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.AddLogAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.AddLogResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class AddLogHandler extends AbstractActionHandler<AddLogAction, AddLogResult> {

    public AddLogHandler() {
        super(AddLogAction.class);
    }

    @Autowired
    LogEntryService logService;

    @Override
    public AddLogResult execute(AddLogAction addLogAction, ExecutionContext executionContext) throws ActionException {
        AddLogResult result = new AddLogResult();

        List<LogEntry> logEntries = new ArrayList<LogEntry>();

        for (GWTLogEntry gwtLogEntry:
                addLogAction.getMessages()) {
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

        result.setUuid(logService.update(logEntries, addLogAction.getOldUUID()));
        return result;
    }

    @Override
    public void undo(AddLogAction addLogAction, AddLogResult addLogResult, ExecutionContext executionContext) throws ActionException {

    }
}
