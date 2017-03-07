package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN')")
public class AuditArchiveHandler extends AbstractActionHandler<AuditArchiveAction, AuditArchiveResult> {
    private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    };

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

        /*Calendar c = Calendar.getInstance();
        c.setTime(action.getLogSystemFilter().getToSearchDate());
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        action.getLogSystemFilter().setToSearchDate(c.getTime());*/

        long recordsCount = auditService.getCountRecords(action.getLogSystemFilter(), userInfo);
        if (recordsCount==0){
            result.setException(true);
//            throw new ServiceException("В журнале аудита отсутствуют записи за выбранный период.");
            return result;
        }
        Logger logger = new Logger();

        Map<String, Object> params = new HashMap<String, Object>();
        String key = LockData.LockObjects.LOG_SYSTEM_BACKUP.name();
        params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
        params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
        params.put(AuditService.AsyncNames.LOG_FILTER.name(), action.getLogSystemFilter());
        params.put(AuditService.AsyncNames.LOG_COUNT.name(), recordsCount);
        Date firstLogDate = auditService.getFirstDateOfLog();
        params.put(AuditService.AsyncNames.LOG_FIRST_DATE.name(), firstLogDate);
        if ((lockData = lockDataService.lock(key, userInfo.getUser().getId(),
                String.format(LockData.DescriptionTemplate.LOG_SYSTEM_BACKUP.getText(), firstLogDate != null ? SDF.get().format(firstLogDate):"", SDF.get().format(action.getLogSystemFilter().getToSearchDate())),
                LockData.State.IN_QUEUE.getText())) == null) {
            try {
                lockData = lockDataService.getLock(key);
                params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                /*String uuid = blobDataService.get(userInfo);*/
                lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
				Long asyncTaskTypeId = PropertyLoader.isProductionMode() ? ReportType.ARCHIVE_AUDIT.getAsyncTaskTypeId() : ReportType.ARCHIVE_AUDIT.getDevModeAsyncTaskTypeId();
                BalancingVariants balancingVariant = asyncManager.checkCreate(asyncTaskTypeId, params);
                asyncManager.executeAsync(asyncTaskTypeId, params, balancingVariant);
				LockData.LockQueues queue = LockData.LockQueues.getById(balancingVariant.getId());
                lockDataService.updateQueue(key, lockData.getDateLock(), queue);
                logger.info(String.format("Задание на архивацию журнала аудита (до даты: %s) поставлено в очередь на формирование.", SDF.get().format(action.getLogSystemFilter().getToSearchDate())));
                return result;
            } catch (Exception e) {
                lockDataService.unlock(key, userInfo.getUser().getId());
                if (e instanceof ServiceLoggerException) {
                    throw (ServiceLoggerException) e;
                } else {
                    throw new ActionException(e);
                }
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
