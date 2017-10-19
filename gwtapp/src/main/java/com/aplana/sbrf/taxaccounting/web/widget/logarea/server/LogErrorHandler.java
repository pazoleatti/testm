package com.aplana.sbrf.taxaccounting.web.widget.logarea.server;

import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.LogErrorAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.LogErrorResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_CONF', 'F_ROLE_CONF', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class LogErrorHandler extends AbstractActionHandler<LogErrorAction, LogErrorResult> {

    @Autowired
    PrintingService printingService;

    @Autowired
    BlobDataService blobDataService;

    public LogErrorHandler() {
        super(LogErrorAction.class);
    }

    @Override
    public LogErrorResult execute(LogErrorAction logErrorAction, ExecutionContext executionContext) throws ActionException {

        LogErrorResult result = new LogErrorResult();

        List<LogEntry> logEntries = new ArrayList<LogEntry>();

        for (GWTLogEntry gwtLogEntry :
                logErrorAction.getLogEntries()) {
            LogEntry logEntry = new LogEntry();
            logEntry.setLogId(gwtLogEntry.getLogId());
            logEntry.setLevel(gwtLogEntry.getLevel());
            logEntry.setMessage(gwtLogEntry.getMessage());
            logEntry.setObject(gwtLogEntry.getObject());
            logEntry.setOrd(gwtLogEntry.getOrd());
            logEntry.setType(gwtLogEntry.getType());
            logEntry.setDate(LocalDateTime.fromDateFields(gwtLogEntry.getDate()));

            logEntries.add(logEntry);
        }

        result.setUuid(printingService.generateExcelLogEntry(logEntries));
        return result;
    }

    @Override
    public void undo(LogErrorAction logErrorAction, LogErrorResult logErrorResult, ExecutionContext executionContext)
            throws ActionException {
    }
}
