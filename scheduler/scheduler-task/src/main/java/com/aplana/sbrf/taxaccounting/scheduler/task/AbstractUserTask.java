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

    private static final Log LOG = LogFactory.getLog(AbstractUserTask.class);

    @Autowired
    LockDataService lockDataService;

    protected String lockId;

    @Override
    public void execute(Map<String, TaskParam> params, int userId, long taskId) throws TaskExecutionException {
        lockId = LockData.LockObjects.SCHEDULER_TASK + "_" + taskId;
        LockData lockData;
        if ((lockData = lockDataService.lock(lockId, TAUser.SYSTEM_USER_ID,
                String.format(LockData.DescriptionTemplate.SCHEDULER_TASK.getText(), getTaskName()))) == null) {
            try {
                LOG.info("Планировщиком запущена задача \"" + getTaskName() + "\"");
                executeBusinessLogic(params, userId);
                LOG.info("Задача планировщика \"" + getTaskName() + "\" успешно завершена");
            } finally {
                lockDataService.unlock(lockId, TAUser.SYSTEM_USER_ID, true);
            }
        } else {
            LOG.info("Задача планировщика \"" + getTaskName() + "\" уже выполняется. Дата начала выполнения: " + lockData.getDateLock());
        }
    }

    /**
     * Метод реализующий логику задачи. Вызывается планировщиком.
     * @param params параметрвы выполнения задачи
     * @throws com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException
     */
    protected abstract void executeBusinessLogic(Map<String, TaskParam> params, int userId) throws TaskExecutionException;
}
