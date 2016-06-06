package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.AsyncTaskManagerService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * @author lhaziev
 */
@Service
public class AsyncTaskManagerServiceImpl implements AsyncTaskManagerService{
    private static final Log LOG = LogFactory.getLog(AsyncTaskManagerServiceImpl.class);

    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private TAUserService userService;
    @Autowired
    private AsyncManager asyncManager;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        }
    };

    public static final String LOCK_INFO_MSG = "Запрашиваемая операция \"%s\" уже запущена %s пользователем %s. Вы добавлены в список получателей оповещения о выполнении данной операции.";
    public static final String CREATE_TASK = "Операция \"%s\" поставлена в очередь на исполнение";
    public static final String CANCEL_MSG = "Запрашиваемая операция \"%s\" уже запущена (находится в очереди на выполнение). Отменить задачу и создать новую?";
    public static final String RESTART_MSG = "Запрашиваемая операция \"%s\" уже запущена (выполняется Системой). При ее отмене задача выполнится до конца, но результат выполнения не будет сохранен. Отменить задачу и создать новую?";


    @Override
    public Pair<Boolean, String> restartTask(String keyTask, String taskName, TAUserInfo userInfo, boolean force, Logger logger) {
        LockData lockDataTask = lockDataService.getLock(keyTask);
        if (lockDataTask != null && lockDataTask.isAsync() && lockDataTask.getUserId() == userInfo.getUser().getId()) {
            if (force) {
                // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                lockDataService.interruptTask(lockDataTask, userInfo.getUser().getId(), false, "Выполнен перезапуск задачи");
            } else {
                // вызов диалога
                String restartMsg = LockData.State.IN_QUEUE.getText().equals(lockDataTask.getState()) ?
                        String.format(CANCEL_MSG, taskName):
                        String.format(RESTART_MSG, taskName);
                return new Pair<Boolean, String>(true, restartMsg);
            }
        } else if (lockDataTask != null && lockDataTask.isAsync()) {
            try {
                lockDataService.addUserWaitingForLock(lockDataTask.getKey(), userInfo.getUser().getId());
                logger.info(String.format(LOCK_INFO_MSG,
                        taskName,
                        sdf.get().format(lockDataTask.getDateLock()),
                        userService.getUser(lockDataTask.getUserId()).getName()));
            } catch (ServiceException e) {
            }
            return new Pair<Boolean, String>(false, null);
        } else if (lockDataTask != null) {
            throw new ServiceLoggerException("Невозможно запустить задачу. Заблокировано операцией: \"%s\"", lockDataTask.getDescription());
        }
        return null;
    }

    @Override
    public void createTask(String keyTask, ReportType reportType, Map<String, Object> params, boolean cancelTask, boolean isProductionMode, TAUserInfo userInfo, Logger logger, AsyncTaskHandler handler) {
        params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
        params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), keyTask);
        // Шаги 1, 2, 5
        BalancingVariants balancingVariant;
        try {
            LOG.info(String.format("Определение очереди для задачи с ключом %s", keyTask));
            balancingVariant = asyncManager.checkCreate(
					isProductionMode ? reportType.getAsyncTaskTypeId() : reportType.getDevModeAsyncTaskTypeId(),
					params);
        } catch (AsyncTaskException e) {
            int i = ExceptionUtils.indexOfThrowable(e, ServiceLoggerException.class);
            if (i != -1) {
                throw (ServiceLoggerException)ExceptionUtils.getThrowableList(e).get(i);
            }
            throw new ServiceException(e.getMessage(), e);
        }
        // Шаг 3
        LOG.info(String.format("Выполнение проверок перед запуском для задачи с ключом %s", keyTask));
        if (!cancelTask && handler.checkExistTask(reportType, userInfo, logger)) {
            LOG.info(String.format("Найдены запущенные задач, по которым требуется удалить блокировку для задачи с ключом %s", keyTask));
            handler.executePostCheck();
        } else {
            LOG.info(String.format("Создание блокировки для задачи с ключом %s", keyTask));
            LockData lockData = handler.createLock(keyTask, reportType, userInfo);
            if (lockData == null) {
                try {
                    // Шаг 4
                    handler.interruptTask(reportType, userInfo);

                    // Шаг 7
                    lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());

                    // Шаг 6
                    LOG.info(String.format("Постановка в очередь задачи с ключом %s", keyTask));
                    lockData = lockDataService.getLock(keyTask);
                    params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                    LockData.LockQueues queue = LockData.LockQueues.getById(balancingVariant.getId());
                    lockDataService.updateQueue(keyTask, lockData.getDateLock(), queue);
                    asyncManager.executeAsync(
							isProductionMode ? reportType.getAsyncTaskTypeId() : reportType.getDevModeAsyncTaskTypeId(),
							params, balancingVariant);

                    // Шаг 8
                    logger.info(String.format(CREATE_TASK, handler.getTaskName(reportType, userInfo)));
                } catch (Exception e) {
                    lockDataService.unlock(keyTask, userInfo.getUser().getId());
                    int i = ExceptionUtils.indexOfThrowable(e, ServiceLoggerException.class);
                    if (i != -1) {
                        throw (ServiceLoggerException)ExceptionUtils.getThrowableList(e).get(i);
                    }
                    throw new ServiceException(e.getMessage(), e);
                }
            } else {
                LOG.info(String.format("Уже существует блокировка задачи с ключом %s", keyTask));
                try {
                    lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());
                    logger.info(String.format(LOCK_INFO_MSG,
                            handler.getTaskName(reportType, userInfo),
                            sdf.get().format(lockData.getDateLock()),
                            userService.getUser(lockData.getUserId()).getName()));
                } catch (ServiceException e) {
                }
            }
        }
    }
}
