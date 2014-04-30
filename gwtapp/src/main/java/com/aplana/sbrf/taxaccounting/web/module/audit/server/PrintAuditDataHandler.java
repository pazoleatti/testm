package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
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
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class PrintAuditDataHandler extends AbstractActionHandler<PrintAuditDataAction, PrintAuditDataResult> {

    @Autowired
    BlobDataService blobDataService;
    @Autowired
    AuditService auditService;
    @Autowired
    PrintingService printingService;
    @Autowired
    SecurityService securityService;

    public PrintAuditDataHandler() {
        super(PrintAuditDataAction.class);
    }

    @Override
    public PrintAuditDataResult execute(PrintAuditDataAction action, ExecutionContext executionContext) throws ActionException {
        try {
            TAUserInfo userInfo = securityService.currentUserInfo();
            PagingResult<LogSearchResultItem> records;
            if (userInfo.getUser().hasRole("ROLE_ADMIN"))
                records = auditService.getLogsByFilter(action.getLogSystemFilter().convertTo());
            else
                records = auditService.getLogsBusiness(action.getLogSystemFilter().convertTo(), userInfo);
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
