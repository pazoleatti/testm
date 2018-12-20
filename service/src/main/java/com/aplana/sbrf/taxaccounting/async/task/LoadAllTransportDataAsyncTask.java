package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Загрузка из каталога загрузки
 */
@Component("LoadAllTransportDataAsyncTask")
public class LoadAllTransportDataAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LoadRefBookDataService loadRefBookDataService;


    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.LOAD_ALL_TRANSPORT_DATA;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo userInfo, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return AsyncQueue.LONG;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) {
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        String key = LockData.LockObjects.CONFIGURATION_PARAMS.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        lockDataService.lock(key, userInfo.getUser().getId(),
                DescriptionTemplate.CONFIGURATION_PARAMS.getText());
        try {
            logger.info("Номер загрузки: %s", taskData.getId());
            //Импорт справочника ФИАС
            loadRefBookDataService.importRefBookFias(userInfo, null, logger, taskData.getId());
        } finally {
            lockDataService.unlock(key, userInfo.getUser().getId());
        }

        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return "Загрузка файлов завершена";
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return "Произошла непредвиденная ошибка при загрузке файлов";
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return getAsyncTaskType().getDescription();
    }

    @Override
    public LockData establishLock(String lockKey, TAUserInfo user, Map<String, Object> params) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean prohibitiveLockExists(Map<String, Object> params, Logger logger) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
