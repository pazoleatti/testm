package com.aplana.sbrf.taxaccounting.web.widget.logarea.server;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
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

        try {
            String filePath = printingService.generateExcelLogEntry(logErrorAction.getLogEntries());
            InputStream fileInputStream = new FileInputStream(filePath);
            String uuid = blobDataService.createTemporary(fileInputStream, "Список_ошибок.xlsx");

            LogErrorResult result = new LogErrorResult();
            result.setUuid(uuid);
            return result;
        } catch (FileNotFoundException e) {
            throw new ServiceException("Проблема при формировании отчета об ошибках. ", e);
        }
    }

    @Override
    public void undo(LogErrorAction logErrorAction, LogErrorResult logErrorResult, ExecutionContext executionContext) throws ActionException {

    }
}
