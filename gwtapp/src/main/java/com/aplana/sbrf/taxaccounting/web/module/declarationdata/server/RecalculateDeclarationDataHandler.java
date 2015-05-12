package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RecalculateDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RecalculateDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class RecalculateDeclarationDataHandler extends AbstractActionHandler<RecalculateDeclarationDataAction, RecalculateDeclarationDataResult> {
    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;

    public RecalculateDeclarationDataHandler() {
        super(RecalculateDeclarationDataAction.class);
    }

    @Override
    public RecalculateDeclarationDataResult execute(RecalculateDeclarationDataAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        try {
            declarationDataService.preCalculationCheck(logger, action.getDeclarationId(), userInfo);
        } catch (Exception e) {
            String uuid;
            if (e instanceof ServiceLoggerException) {
                uuid = ((ServiceLoggerException) e).getUuid();
            } else {
                uuid = logEntryService.save(logger.getEntries());
            }
            throw new ServiceLoggerException("%s. Обнаружены фатальные ошибки", uuid, !TaxType.DEAL.equals(action.getTaxType())?"Декларация не может быть сформирована":"Уведомление не может быть сформировано");
        }
        int userId = userInfo.getUser().getId();
        RecalculateDeclarationDataResult result = new RecalculateDeclarationDataResult();
        String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), ReportType.XML_DEC);
        LockData lockData = lockDataService.lock(key, userId,
                declarationDataService.getDeclarationFullName(action.getDeclarationId(), null),
                LockData.State.IN_QUEUE.getText(),
                lockDataService.getLockTimeout(LockData.LockObjects.DECLARATION_DATA));
        if (lockData == null) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("declarationDataId", action.getDeclarationId());
                params.put("docDate", action.getDocDate());
                params.put(AsyncTask.RequiredParams.USER_ID.name(), userId);
                params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                lockData = lockDataService.getLock(key);
                params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                try {
                    declarationDataService.deleteReport(action.getDeclarationId(), false);
                    // отменяем задания на формирование XLSX и PDF
                    String keyPdf = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), ReportType.PDF_DEC);
                    if (lockDataService.getLock(keyPdf) != null) {
                        List<Integer> waitingUserIds = lockDataService.getUsersWaitingForLock(keyPdf);
                        for (int waitingUserId : waitingUserIds)
                            if (waitingUserId != userId) lockDataService.addUserWaitingForLock(key, waitingUserId);
                        lockDataService.unlock(keyPdf, 0, true);
                    }
                    lockDataService.unlock(declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), ReportType.EXCEL_DEC), 0, true);
                    // ставим задачу в очередь
                    lockDataService.addUserWaitingForLock(key, userId);
                    BalancingVariants balancingVariant = asyncManager.executeAsync(ReportType.XML_DEC.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                    lockDataService.updateQueue(key, lockData.getDateLock(), balancingVariant.getName());
                } catch (AsyncTaskException e) {
                    lockDataService.unlock(key, userId);
                    logger.error("Ошибка при постановке в очередь задачи формирования декларации.");
                }
                if (!logger.containsLevel(LogLevel.ERROR)) {
                    logger.info("%s в очередь на формирование.", !TaxType.DEAL.equals(action.getTaxType())?"Декларация поставлена":"Уведомление поставлено");
                }
                result.setUuid(logEntryService.save(logger.getEntries()));
            } catch(Exception e) {
                try {
                    lockDataService.unlock(key, userId);
                } catch (ServiceException e2) {
                    if (PropertyLoader.isProductionMode() || !(e instanceof RuntimeException)) { // в debug-режиме не выводим сообщение об отсутсвии блокировки, если оня снята при выбрасывании исключения
                        throw new ActionException(e2);
                    }
                }
                if (e instanceof ServiceLoggerException) {
                    throw new ServiceLoggerException(e.getMessage(), ((ServiceLoggerException) e).getUuid());
                } else {
                    throw new ActionException(e);
                }
            }
        } else {
            throw new ActionException("Декларация заблокирована и не может быть изменена. Попробуйте выполнить операцию позже");
        }
        return result;
    }

    @Override
    public void undo(RecalculateDeclarationDataAction action, RecalculateDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
