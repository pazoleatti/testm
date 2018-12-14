package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.CreateApplication2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Формирование приложения 2
 */
@Component("CreateApplication2AsyncTask")
public class CreateApplication2AsyncTask extends AbstractAsyncTask {

    @Autowired
    private CreateApplication2Service createApplication2Service;

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return String.format(AsyncTaskType.CREATE_APPLICATION_2.getDescription(), (int) params.get("reportYear"));
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException {
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        String uuid = null;
        try {
            uuid = createApplication2Service.performCreateApplication2((int) taskData.getParams().get("reportYear"), userInfo, logger);
        } catch (IOException e) {
            return new BusinessLogicResult(false, null);
        }

        if (logger.containsLevel(LogLevel.ERROR)) {
            return new BusinessLogicResult(false, null);
        }
        return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return String.format("Сформирован файл Приложения 2 для декларации по налогу на прибыль за %s год.", (int) taskData.getParams().get("reportYear"));
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return String.format("Не сформирован файл Приложения 2 для декларации по налогу на прибыль за %s год. Имеются фатальные ошибки", (int) taskData.getParams().get("reportYear"));
    }

    @Override
    protected AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return AsyncQueue.LONG;
    }

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.CREATE_APPLICATION_2;
    }

    @Override
    public LockData lockObject(String lockKey, TAUserInfo user, Map<String, Object> params) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean checkLocks(Map<String, Object> params, Logger logger) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
