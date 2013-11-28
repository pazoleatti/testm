package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.server;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.PrintLogBusinessAction;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.PrintLogBusinessResult;
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
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class PrintLogBusinessHandler extends AbstractActionHandler<PrintLogBusinessAction, PrintLogBusinessResult> {
    public PrintLogBusinessHandler() {
        super(PrintLogBusinessAction.class);
    }

    @Autowired
    PrintingService printingService;

    @Autowired
    BlobDataService blobDataService;

    @Autowired
    LogBusinessService logBusinessService;

    @Autowired
    private SecurityService securityService;

    @Override
    public PrintLogBusinessResult execute(PrintLogBusinessAction action, ExecutionContext context) throws ActionException {
        try {
            PagingResult<LogSearchResultItem> records = logBusinessService.getLogsBusiness(securityService.currentUserInfo(), action.getFilterValues());
            String filePath = printingService.generateExcelLogSystem(records);
            InputStream fileInputStream = new FileInputStream(filePath);

            PrintLogBusinessResult result = new PrintLogBusinessResult();
            result.setUuid(blobDataService.createTemporary(fileInputStream, "Журнал_аудита.xlsx"));
            return result;
        } catch (FileNotFoundException e) {
            throw new ServiceException("Проблема при генерации отчета журнала аудита." , e);
        }
    }

    @Override
    public void undo(PrintLogBusinessAction action, PrintLogBusinessResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
