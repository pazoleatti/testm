package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.AuditArchiveAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.AuditArchiveResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class AuditArchiveHandler extends AbstractActionHandler<AuditArchiveAction, AuditArchiveResult> {

    @Autowired
    AuditService auditService;

    @Autowired
    PrintingService printingService;

    @Autowired
    BlobDataService blobDataService;

    @Autowired
    SecurityService securityService;

    public AuditArchiveHandler() {
        super(AuditArchiveAction.class);
    }

    @Override
    public AuditArchiveResult execute(AuditArchiveAction action, ExecutionContext context) throws ActionException {
        AuditArchiveResult result = new AuditArchiveResult();
        PagingResult<LogSystemSearchResultItem> records = auditService.getLogsByFilter(action.getLogSystemFilter());
        if (records.isEmpty())
            throw new ServiceException("Нет записей за указанную дату.");
        File filePath = new File(printingService.generateAuditCsv(records));
        try {
            String uuid = blobDataService.createTemporary(new FileInputStream(filePath), filePath.getName());
            result.setUuid(uuid);
            auditService.removeRecords(records, securityService.currentUserInfo());
            return result;
        } catch (FileNotFoundException e) {
            throw new ServiceException("Возникла проблема при считывании файла, файл не найден.");
        }finally {
            filePath.delete();
        }
    }

    @Override
    public void undo(AuditArchiveAction action, AuditArchiveResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
