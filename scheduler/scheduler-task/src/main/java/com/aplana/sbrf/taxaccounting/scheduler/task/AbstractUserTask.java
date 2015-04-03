package com.aplana.sbrf.taxaccounting.scheduler.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Абстрактная реализация задачи планировщика.
 * В ней реализована общая часть логики взаимодействия с блокировками объектов, для которых выполняется бизнес-логика конкретных задач
 * @author dloshkarev
 */
public abstract class AbstractUserTask implements UserTask {

    protected static final Log log = LogFactory.getLog(AbstractUserTask.class);

    @Autowired
    LockDataService lockDataService;

    @Override
    public void execute(Map<String, TaskParam> params, int userId, long taskId) throws TaskExecutionException {
        String key = LockData.LockObjects.SCHEDULER_TASK + "_" + taskId;
        LockData lockData;
        if ((lockData = lockDataService.lock(key, TAUser.SYSTEM_USER_ID,
                lockDataService.getLockTimeout(LockData.LockObjects.SCHEDULER_TASK))) == null) {
            try {
                executeBusinessLogic(params, userId);
            } finally {
                lockDataService.unlock(key, TAUser.SYSTEM_USER_ID, true);
            }
        } else {
            log.info("Задача планировщика \"" + getTaskName() + "\" уже выполняется. Дата начала выполнения: " + lockData.getDateLock());
        }
    }

    /**
     * Метод реализующий логику задачи. Вызывается планировщиком.
     * @param params параметрвы выполнения задачи
     * @throws com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException
     */
    protected abstract void executeBusinessLogic(Map<String, TaskParam> params, int userId) throws TaskExecutionException;
}
