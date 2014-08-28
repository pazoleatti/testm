package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
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
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class AuditArchiveHandler extends AbstractActionHandler<AuditArchiveAction, AuditArchiveResult> {

    @Autowired
    AuditService auditService;

    @Autowired
    PrintingService printingService;

    @Autowired
    BlobDataService blobDataService;

    @Autowired
    SecurityService securityService;

    @Autowired
    TAUserService taUserService;

    @Autowired
    private LogEntryService logEntryService;

    public AuditArchiveHandler() {
        super(AuditArchiveAction.class);
    }

    @Override
    public AuditArchiveResult execute(AuditArchiveAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        AuditArchiveResult result = new AuditArchiveResult();
        LockData lockData = auditService.lock(userInfo);
        Logger logger = new Logger();
        if (lockData == null) {
            try {
                PagingResult<LogSearchResultItem> records = auditService.getLogsByFilter(action.getLogSystemFilter());
                if (records.isEmpty())
                    throw new ServiceException("Нет записей за указанную дату.");
                File filePath = new File(printingService.generateAuditCsv(records));
                try {
                    String uuid = blobDataService.createTemporary(new FileInputStream(filePath), filePath.getName());
                    result.setFileUuid(uuid);
                    auditService.removeRecords(records, securityService.currentUserInfo());
                    result.setCountOfRemoveRecords(records.getTotalCount());
                    return result;
                } catch (FileNotFoundException e) {
                    throw new ServiceException("Возникла проблема при считывании файла, файл не найден.");
                } finally {
                    filePath.delete();
                }
            } finally{
                auditService.unlock(userInfo);
            }
        } else {
            TAUser user = taUserService.getUser((int) lockData.getUserId());
            logger.error("Операция недоступна, так как она выполняется сейчас пользователем " + user.getName() + ". Повторите архивацию позже.");
            result.setException(true);
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public void undo(AuditArchiveAction action, AuditArchiveResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
