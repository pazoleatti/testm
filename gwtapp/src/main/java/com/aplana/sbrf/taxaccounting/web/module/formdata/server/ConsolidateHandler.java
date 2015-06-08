package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.ConsolidateAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.ConsolidateResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class ConsolidateHandler extends AbstractActionHandler<ConsolidateAction, ConsolidateResult> {

    @Autowired
    private FormDataService formDataService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private LockDataService lockDataService;

    public ConsolidateHandler() {
        super(ConsolidateAction.class);
    }

    @Override
    public ConsolidateResult execute(ConsolidateAction action, ExecutionContext executionContext) throws ActionException {
        final ReportType reportType = ReportType.CONSOLIDATE_FD;
        ConsolidateResult result = new ConsolidateResult();
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormDataId());
        if (lockType == null || reportType.equals(lockType.getFirst())) {
            String keyTask = formDataService.generateTaskKey(action.getFormDataId(), reportType);
            LockData lockDataTask = lockDataService.getLock(keyTask);
            if (lockDataTask != null && lockDataTask.getUserId() == userInfo.getUser().getId()) {
                if (action.isForce()) {
                    // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                    lockDataService.interruptTask(lockDataTask, userInfo.getUser().getId(), false);
                } else {
                    result.setLock(true);
                    lockDataService.lockInfo(lockType.getSecond(), logger);
                    result.setUuid(logEntryService.save(logger.getEntries()));
                    return result;
                }
            } else if (lockDataTask != null) {
                try {
                    lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());
                } catch (ServiceException e) {
                }
                result.setLock(false);
                logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.getTaxType().getTaxText());
                result.setUuid(logEntryService.save(logger.getEntries()));
                return result;
            }
            if (lockDataService.lock(keyTask, userInfo.getUser().getId(),
                    formDataService.getFormDataFullName(action.getFormDataId(), null, reportType),
                    LockData.State.IN_QUEUE.getText(),
                    lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA)) == null) {
                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("formDataId", action.getFormDataId());
                    params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                    params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), keyTask);
                    LockData lockData = lockDataService.getLock(keyTask);
                    params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                    lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());
                    BalancingVariants balancingVariant = asyncManager.executeAsync(reportType.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                    lockDataService.updateQueue(keyTask, lockData.getDateLock(), balancingVariant);
                    logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.getTaxType().getTaxText());
                    result.setLock(false);
                } catch (Exception e) {
                    lockDataService.unlock(keyTask, userInfo.getUser().getId());
                    if (e instanceof ServiceLoggerException) {
                        throw (ServiceLoggerException) e;
                    } else {
                        throw new ActionException(e);
                    }
                }
            } else {
                throw new ActionException("Не удалось запустить консолидацию. Попробуйте выполнить операцию позже");
            }
        } else {
            formDataService.locked(lockType.getSecond(), logger);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(ConsolidateAction consolidateAction, ConsolidateResult consolidateResult, ExecutionContext executionContext) throws ActionException {

    }
}
