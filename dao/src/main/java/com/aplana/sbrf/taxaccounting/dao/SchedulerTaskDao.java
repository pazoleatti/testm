package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskModel;

import java.util.List;

/**
 * DAO-Интерфейс для работы с планировщиком
 */
public interface SchedulerTaskDao {

    /**
     * Получение данных по id задачи
     *
     * @param taskId идентификатор задачи
     * @return объект {@link SchedulerTaskData}
     */
    SchedulerTaskData fetchOneSchedulerTask(Long taskId);

    /**
     * Получить все задачи планировщика
     *
     * @return Список задач {@link SchedulerTaskData}
     */
    List<SchedulerTaskData> fetchAllSchedulerTasks();

    /**
     * Получение параметров всех задач планировщика
     *
     * @param pagingParams параметры пагиинации
     * @return Список задач {@link SchedulerTaskModel}
     */
    PagingResult<SchedulerTaskModel> fetchAllSchedulerTasks(PagingParams pagingParams);

    /**
     * Обновление параметров задачи планировщика
     *
     * @param taskData объект задачи
     */
    void updateTask(SchedulerTaskData taskData);

    /**
     * Обновление даты последнего запуска задачи
     *
     * @param taskId идентификатор задачи
     */
    void updateTaskStartDate(long taskId);

    /**
     * Изменение признака активности задач
     *
     * @param active признак активности
     * @param ids    список идентификаторов задач
     */
    void setActiveSchedulerTask(boolean active, List<Long> ids);
}
