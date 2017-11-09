package com.aplana.sbrf.taxaccounting.web.widget.logarea.server;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.GetLogEntriesAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.GetLogEntriesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_CONF', 'F_ROLE_CONF', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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

        PagingParams pagingParams = PagingParams.getInstance(action.getStart() / action.getLength() + 1, action.getLength());
        PagingResult<LogEntry> logEntries = logEntryService.fetch(action.getUuid(), pagingParams);

        List<GWTLogEntry> listGwtLogEntries = new ArrayList<GWTLogEntry>();

        for (LogEntry logEntry :
                logEntries) {
            GWTLogEntry gwtLogEntry = new GWTLogEntry();
            gwtLogEntry.setLogId(logEntry.getLogId());
            gwtLogEntry.setLevel(logEntry.getLevel());
            gwtLogEntry.setMessage(logEntry.getMessage());
            gwtLogEntry.setObject(logEntry.getObject());
            gwtLogEntry.setOrd(logEntry.getOrd());
            gwtLogEntry.setType(logEntry.getType());
            gwtLogEntry.setDate(logEntry.getDate().toDate());

            listGwtLogEntries.add(gwtLogEntry);
        }

        PagingResult<GWTLogEntry> gwtLogEntries = new PagingResult<GWTLogEntry>(listGwtLogEntries, logEntries.getTotalCount());

        result.setLogEntries(gwtLogEntries);
        result.setLogEntriesCount(logEntryService.getLogCount(action.getUuid()));

        return result;
    }

    @Override
    public void undo(GetLogEntriesAction action, GetLogEntriesResult result, ExecutionContext context) throws ActionException {
        // Не требуется
    }
}