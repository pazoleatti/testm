package com.aplana.sbrf.taxaccounting.scheduler.core.persistence;

import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskPersistenceException;
import com.aplana.sbrf.taxaccounting.scheduler.core.entity.TaskContextEntity;

import java.util.List;

/**
 * Слой для работы с бд
 * @author dloshkarev
 */
public interface TaskPersistenceService {
    /**
     * Сохраняет данные контекста в бд
     * @param context контекст задачи
     */
    void saveContext(TaskContextEntity context) throws TaskPersistenceException;

    /**
     * Получает контект задачи по ее идентификатору
     * @param taskId идентификатор задачи
     * @return контекст задачи
     * @throws com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskPersistenceException
     */
    TaskContextEntity getContextByTaskId(Long taskId) throws TaskPersistenceException;

    /**
     * Получает все сохраненные контексты задач
     * @return список контекстов
     * @throws com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskPersistenceException
     */
    List<TaskContextEntity> getAllContexts() throws TaskPersistenceException;

    /**
     * Удаляет контекст задачи
     * @param taskId идентификатор задачи
     * @throws com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskPersistenceException
     */
    void deleteContextByTaskId(Long taskId) throws TaskPersistenceException;
}
