package com.aplana.sbrf.taxaccounting.web.module.ifrs.server;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.IfrsDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.CalculateIfrsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.CalculateIfrsDataResult;
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

/**
 * @author lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class CalculateIfrsDataHandler extends AbstractActionHandler<CalculateIfrsDataAction, CalculateIfrsDataResult> {

    @Autowired
    private IfrsDataService ifrsDataService;
    @Autowired
    SecurityService securityService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    PeriodService periodService;

    public CalculateIfrsDataHandler() {
        super(CalculateIfrsDataAction.class);
    }

    @Override
    public CalculateIfrsDataResult execute(CalculateIfrsDataAction action, ExecutionContext executionContext) throws ActionException {
        CalculateIfrsDataResult result = new CalculateIfrsDataResult();
        IfrsData ifrsData = ifrsDataService.get(action.getReportPeriodId());
        if (ifrsData != null & ifrsData.getBlobDataId() != null) {
            result.setBlobDataId(ifrsData.getBlobDataId());
            return result;
        }
        TAUserInfo userInfo = securityService.currentUserInfo();
        ReportPeriod reportPeriod = periodService.getReportPeriod(action.getReportPeriodId());
        Logger logger = new Logger();
        String key = ifrsDataService.generateTaskKey(action.getReportPeriodId());
        LockData lockData = lockDataService.lock(key, userInfo.getUser().getId(),
                LockData.State.IN_QUEUE.getText(),
                String.format(LockData.DescriptionTemplate.IFRS.getText(), reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear()));
        if (lockData == null) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("reportPeriodId", action.getReportPeriodId());
                params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                lockData = lockDataService.getLock(key);
                params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());

                if (!ifrsDataService.check(logger, action.getReportPeriodId())) {
                    lockDataService.unlock(key, userInfo.getUser().getId());
                    result.setError(true);
                    result.setUuid(logEntryService.save(logger.getEntries()));
                    return result;
                }

                try {
                    // ставим задачу в очередь
                    List<Integer> userIds = ifrsDataService.getIfrsUsers();
                    for(Integer userId: userIds) {
                        lockDataService.addUserWaitingForLock(key, userId);
                    }
					Long asyncTaskTypeId = PropertyLoader.isProductionMode() ? ReportType.ZIP_IFRS.getAsyncTaskTypeId() : ReportType.ZIP_IFRS.getDevModeAsyncTaskTypeId();
                    BalancingVariants balancingVariant = asyncManager.checkCreate(asyncTaskTypeId, params);
                    asyncManager.executeAsync(asyncTaskTypeId, params, balancingVariant);
					LockData.LockQueues queue = LockData.LockQueues.getById(balancingVariant.getId());
                    lockDataService.updateQueue(key, lockData.getDateLock(), queue);
                } catch (AsyncTaskException e) {
                    lockDataService.unlock(key, userInfo.getUser().getId());
                    logger.error("Ошибка при постановке в очередь асинхронной задачи формирования отчета");
                }
            } catch (Exception e) {
                if (PropertyLoader.isProductionMode() || !(e.getClass().equals(RuntimeException.class))) {
                    lockDataService.unlock(key, userInfo.getUser().getId());
                }
                if (e instanceof ServiceLoggerException) {
                    throw new ServiceLoggerException(e.getMessage(), ((ServiceLoggerException) e).getUuid());
                } else {
                    throw new ActionException(e);
                }
            }
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            logger.info("Архив с отчетностью для МСФО за %s %s поставлен в очередь на формирование", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
        } else {
            result.setError(true);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CalculateIfrsDataAction action, CalculateIfrsDataResult result, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
