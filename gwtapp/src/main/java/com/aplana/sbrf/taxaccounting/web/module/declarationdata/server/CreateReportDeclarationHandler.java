package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateReportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateReportResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lhaziev
 *
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateReportDeclarationHandler extends AbstractActionHandler<CreateReportAction, CreateReportResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    public CreateReportDeclarationHandler() {
        super(CreateReportAction.class);
    }

    @Override
    public CreateReportResult execute(CreateReportAction action, ExecutionContext executionContext) throws ActionException {
        CreateReportResult result = new CreateReportResult();
        Map<String, Object> params = new HashMap<String, Object>();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        LockData lockData = declarationDataService.lock(action.getDeclarationDataId(), userInfo);
        if (lockData == null) {
            try {
                String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationDataId(), action.getType());
                params.put("declarationDataId", action.getDeclarationDataId());
                params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                LockData lockDataReportTask;
                if ((lockDataReportTask = lockDataService.lock(key, userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME * 24)) == null) {
                    try {
                        String uuid = reportService.getDec(userInfo, action.getDeclarationDataId(), action.getType());
                        if (uuid == null) {
                            params.put(AsyncTask.RequiredParams.LOCK_DATE_END.name(), lockDataService.getLock(key).getDateBefore());
                            lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                            asyncManager.executeAsync(action.getType().getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params, BalancingVariants.SHORT);
                            logger.info(String.format("%s отчет текущей декларации поставлен в очередь на формирование.", action.getType().getName()));
                        } else {
                            result.setExistReport(true);
                            lockDataService.unlock(key, userInfo.getUser().getId());
                        }
                    } catch (Exception e) {
                        lockDataService.unlock(key, userInfo.getUser().getId());
                        throw new ActionException("Ошибка при постановке в очередь асинхронной задачи", e);
                    }
                } else {
                    if (lockDataReportTask.getUserId() != userInfo.getUser().getId()) {
                        try {
                            lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                        } catch (ServiceException e) {
                        }
                    }
                    logger.info(String.format("%s отчет текущей декларации поставлен в очередь на формирование.", action.getType().getName()));
                }
            } finally {
                declarationDataService.unlock(action.getDeclarationDataId(), userInfo);
            }
        } else {
            throw new ActionException("Декларация заблокирована и не может быть изменена. Попробуйте выполнить операцию позже");
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateReportAction searchAction, CreateReportResult searchResult, ExecutionContext executionContext) throws ActionException {

    }
}
