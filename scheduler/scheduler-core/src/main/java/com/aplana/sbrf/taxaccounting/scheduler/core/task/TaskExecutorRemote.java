package com.aplana.sbrf.taxaccounting.scheduler.core.task;

import com.ibm.websphere.scheduler.TaskStatus;

/**
 * Интерфейс, определяющий логику задачи, выполняемой на планировщике WAS
 * @author dloshkarev
 */
public interface TaskExecutorRemote {
    /**
     * Выполнение логики задачи, через вызов конкретного обработчика задачи
     * @param taskStatus параметры задачи
     */
    void process(TaskStatus taskStatus);
}
