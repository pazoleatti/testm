package com.aplana.sbrf.taxaccounting.scheduler.core.service;

import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;

/**
 * Вспомогательный компонент для управления запуском задач
 * @author dloshkarev
 */
public interface TaskService {
    /**
     * Запуск задачи по ее идентификатору. Данные задачи получаются из БД
     * @param taskId идентификатор задачи
     * @throws com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException
     */
    public void startTaskById(Long taskId) throws TaskExecutionException;

    /**
     * Запуск задачи с учетом нового контекста. Фактически однократный запуск новой задачи.
     * @param taskContext контекст задачи
     * @throws com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException
     */
    public void startTaskWithContext(TaskContext taskContext) throws TaskExecutionException;
}
