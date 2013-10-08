package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.*;
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
import java.text.SimpleDateFormat;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class AuditArchiveHandler extends AbstractActionHandler<AuditArchiveAction, AuditArchiveResult> {

    private static final SimpleDateFormat SDF_LOG_NAME = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    private static final String PATTER_LOG_FILE_NAME = "log_<%s>-<%s>";

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
            return null;
        File filePath = new File(printingService.generateAuditCsv(records));
        try {
            String uuid = blobDataService.createTemporary(new FileInputStream(filePath), String.format(PATTER_LOG_FILE_NAME,
                    SDF_LOG_NAME.format(records.get(0).getLogDate()),
                    SDF_LOG_NAME.format(records.get(records.size() - 1).getLogDate())));
            result.setUuid(uuid);
            auditService.removeRecords(records, securityService.currentUserInfo());
        } catch (FileNotFoundException e) {
            throw new ServiceException("Возникла проблема при считывании файла, файл не найден.");
        }finally {
            filePath.delete();
        }
        return result;
    }

    @Override
    public void undo(AuditArchiveAction action, AuditArchiveResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
