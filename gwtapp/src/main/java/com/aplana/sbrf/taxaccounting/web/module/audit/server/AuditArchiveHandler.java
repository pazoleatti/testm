package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.AuditArchiveAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.AuditArchiveResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class AuditArchiveHandler extends AbstractActionHandler<AuditArchiveAction, AuditArchiveResult> {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

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
    private LockDataService lockDataService;

    @Autowired
    LogEntryService logEntryService;

    @Autowired
    private AsyncManager asyncManager;

    public AuditArchiveHandler() {
        super(AuditArchiveAction.class);
    }

    @Override
    public AuditArchiveResult execute(AuditArchiveAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        AuditArchiveResult result = new AuditArchiveResult();
        LockData lockData;
        long recordsCount = auditService.getCountRecords(action.getLogSystemFilter(), userInfo);
        if (recordsCount==0)
            throw new ServiceException("Нет записей за указанную дату.");
        Logger logger = new Logger();

        Map<String, Object> params = new HashMap<String, Object>();
        String key = LockData.LockObjects.LOG_SYSTEM_BACKUP.name();
        params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
        params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
        params.put(AuditService.AsyncNames.LOG_FILTER.name(), action.getLogSystemFilter());
        params.put(AuditService.AsyncNames.LOG_COUNT.name(), recordsCount);
        if ((lockData = lockDataService.lock(key, userInfo.getUser().getId(),
                String.format(LockData.DescriptionTemplate.LOG_SYSTEM_BACKUP.getText(), SDF.format(action.getLogSystemFilter().getToSearchDate())),
                LockData.State.IN_QUEUE.getText(),
                lockDataService.getLockTimeout(LockData.LockObjects.LOG_SYSTEM_BACKUP))) == null) {
            try {
                params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockDataService.getLock(key).getDateLock());
                /*String uuid = blobDataService.get(userInfo);*/
                lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                asyncManager.executeAsync(ReportType.ARCHIVE_AUDIT.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params, BalancingVariants.SHORT);
                logger.info(String.format("Задание на архивацию журнала аудита (до даты: %s) поставлено в очередь на формирование.", SDF.format(action.getLogSystemFilter().getToSearchDate())));
                return result;
            } catch (AsyncTaskException e) {
                lockDataService.unlock(key, userInfo.getUser().getId());
            } finally{
                lockDataService.unlock(key, userInfo.getUser().getId());
            }
        } else {
            if (lockData.getUserId() != userInfo.getUser().getId()) {
                try {
                    lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                } catch(ServiceException e) {
                }
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public void undo(AuditArchiveAction action, AuditArchiveResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
