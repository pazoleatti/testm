package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Проверочный таск
 * @author dloshkarev
 */
@Component
public class TestAsyncTask extends AbstractAsyncTask {

    @Autowired
    TAUserService userService;

    @Autowired
    RefBookFactory refBookFactory;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.TEST;
    }

    @Override
    protected AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return AsyncQueue.SHORT;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        for (int i = 0; i < 20; i ++) {
            System.out.println("TestAsyncTaskImpl started: " + new Date().getTime() + ". Thread: " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return "Тест тест тест!";
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return "Ошибка в тестовой задаче";
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return "Тестовая задача";
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
