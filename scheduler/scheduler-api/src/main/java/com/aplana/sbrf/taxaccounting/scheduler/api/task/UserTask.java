package com.aplana.sbrf.taxaccounting.scheduler.api.task;

import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;

import java.util.Map;

/**
 * Внешний интерфейс, определяющий логику выполнения задачи
 * @author dloshkarev
 */
public interface UserTask {
    /**
     * Метод реализующий логику задачи. Вызывается планировщиком.
     * @param params параметрвы выполнения задачи
     * @throws TaskExecutionException
     */
    void execute(Map<String, TaskParam> params) throws TaskExecutionException;
}
