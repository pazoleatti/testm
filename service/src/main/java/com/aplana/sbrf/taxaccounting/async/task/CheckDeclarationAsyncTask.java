package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Проверка налоговой формы
 */
@Component("CheckDeclarationAsyncTask")
public class CheckDeclarationAsyncTask extends AbstractDeclarationAsyncTask {

    private static final String SUCCESS = "Выполнена проверка налоговой формы: %s";

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;



    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.CHECK_DEC;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        taskData.getParams().put("standardDeclarationDescription", declarationDataService.getStandardDeclarationDescription(declarationDataId));
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        if (declarationData != null) {
            declarationDataService.check(logger, declarationDataId, userInfo, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                    asyncManager.updateState(taskData.getId(), state);
                }
            });
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            return new BusinessLogicResult(false, null);
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        String message = getMessage(taskData, false);
        Exception e = (Exception) taskData.getParams().get("exceptionThrown");
        if (e != null) {
            message = message + String.format(CAUSE, e.toString());
        }
        return message;
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return getMessage(taskData, true);
    }

    private String getMessage(AsyncTaskData taskData, boolean isSuccess) {
        String standardDeclarationDescription = (String) taskData.getParams().get("standardDeclarationDescription");
        if (isSuccess) {
            return String.format(SUCCESS, standardDeclarationDescription);
        } else {
            return String.format(FAIL, "Проверка", standardDeclarationDescription);
        }
    }

    @Override
    public boolean checkLocks(Map<String, Object> params, Logger logger) {
        long declarationDataId = (Long) params.get("declarationDataId");
        String checkKey = declarationDataService.generateAsyncTaskKey(declarationDataId, DeclarationDataReportType.CHECK_DEC);
        String acceptKey = declarationDataService.generateAsyncTaskKey(declarationDataId, DeclarationDataReportType.ACCEPT_DEC);

        if (lockDataService.isLockExists(checkKey, false)) {
            logger.error(getLockExistErrorMessage(declarationDataService.getStandardDeclarationDescription(declarationDataId), checkKey));
            return true;
        }
        if (lockDataService.isLockExists(acceptKey, false)) {
            logger.error(getLockExistErrorMessage(declarationDataService.getStandardDeclarationDescription(declarationDataId), acceptKey));
            return true;
        }

        return false;
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return String.format(getAsyncTaskType().getDescription(),
                declarationDataService.getDeclarationFullName(declarationDataId, getDeclarationDataReportType(userInfo, params)));
    }

    @Override
    public LockData lockObject(String lockKey, TAUserInfo user, Map<String, Object> params) {
        return lockDataService.lock(lockKey, user.getUser().getId(), getDescription(user, params));
    }

}
