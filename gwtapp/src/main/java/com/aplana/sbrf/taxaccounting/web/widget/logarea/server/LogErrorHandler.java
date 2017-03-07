package com.aplana.sbrf.taxaccounting.web.widget.logarea.server;

import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.LogErrorAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.LogErrorResult;
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
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN', 'N_ROLE_CONF', 'F_ROLE_CONF', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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
        result.setUuid(printingService.generateExcelLogEntry(logErrorAction.getLogEntries()));
        return result;
    }

    @Override
    public void undo(LogErrorAction logErrorAction, LogErrorResult logErrorResult, ExecutionContext executionContext)
            throws ActionException {
    }
}
