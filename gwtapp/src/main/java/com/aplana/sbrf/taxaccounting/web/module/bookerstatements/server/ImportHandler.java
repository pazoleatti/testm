package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.BookerStatementsService;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.ImportAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.ImportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Обработка загружаемых данных бух отчетности для подразделения и отчетного периода
 * User: ekuvshinov
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class ImportHandler extends AbstractActionHandler<ImportAction, ImportResult> {
    @Autowired
    BookerStatementsService service;
    @Autowired
    BlobDataService blobDataService;

    public ImportHandler() {
        super(ImportAction.class);
    }

    @Override
    public ImportResult execute(ImportAction importAction, ExecutionContext executionContext) throws ActionException {
        BlobData blobData = blobDataService.get(importAction.getUuid());
        ImportResult importResult = new ImportResult();
        service.importXML(blobData.getName(), blobData.getInputStream(), importAction.getReportPeriodId(), importAction.getTypeId(), importAction.getDepartmentId());
        return importResult;
    }

    @Override
    public void undo(ImportAction importAction, ImportResult importResult, ExecutionContext executionContext) throws ActionException {
        // Не обрабатывается
    }


}
