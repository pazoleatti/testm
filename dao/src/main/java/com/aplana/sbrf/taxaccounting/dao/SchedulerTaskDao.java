package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParam;

import java.util.List;

/**
 * DAO-Интерфейс для работы с планировщиком
 */
public interface SchedulerTaskDao {

    /**
     * Получение данных по id задачи
     * @param taskId
     * @return
     */
    SchedulerTaskData get(Long taskId);

    /**
     * Получить все задачи планировщика
     * @return
     */
    List<SchedulerTaskData> getAll();

    /**
     * Обновление параметров задачи планировщика
     */
    void updateTask(SchedulerTaskData taskData);

    /**
     * Обновить дату последнего запуска задачи
     * @param taskId
     */
    void updateTaskStartDate(long taskId);

    /**
     * Изменение признака активности задач
     * @param active
     * @param ids
     */
    void setActiveSchedulerTask(boolean active, List<Long> ids);
}
