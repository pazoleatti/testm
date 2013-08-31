package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.PrintAuditDataAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.PrintAuditDataResult;
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
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class PrintAuditDataHandler extends AbstractActionHandler<PrintAuditDataAction, PrintAuditDataResult> {

    @Autowired
    BlobDataService blobDataService;

    @Autowired
    AuditService auditService;

    @Autowired
    PrintingService printingService;

    public PrintAuditDataHandler() {
        super(PrintAuditDataAction.class);
    }

    @Override
    public PrintAuditDataResult execute(PrintAuditDataAction printAuditDataAction, ExecutionContext executionContext) throws ActionException {
        try {
            PagingResult<LogSystemSearchResultItem> records = auditService.getLogsByFilter(printAuditDataAction.getLogSystemFilter());
            String filePath = printingService.generateExcelLogSystem(records);
            InputStream fileInputStream = new FileInputStream(filePath);

            PrintAuditDataResult result = new PrintAuditDataResult();
            result.setUuid(blobDataService.createTemporary(fileInputStream, "Журнал_аудита.xlsx"));
            return result;
        } catch (FileNotFoundException e) {
            throw new ServiceException("Проблема при генерации отчета журнала аудита." , e);
        }
    }

    @Override
    public void undo(PrintAuditDataAction printAuditDataAction, PrintAuditDataResult printAuditDataResult, ExecutionContext executionContext) throws ActionException {
        //No implementation
    }
}
