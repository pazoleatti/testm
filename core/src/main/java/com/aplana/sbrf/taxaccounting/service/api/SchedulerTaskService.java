package com.aplana.sbrf.taxaccounting.service.api;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;

import java.util.List;

public interface SchedulerTaskService {
    /**
     * Получение параметров задачи планировщика
     *
     * @param task объект {@link SchedulerTask} задачи
     * @return объект {@link SchedulerTaskData} задачи
     */
    SchedulerTaskData getSchedulerTask(SchedulerTask task);

    /**
     * Получение параметров всех задач планировщика
     *
     * @return список задач {@link SchedulerTaskData}
     */
    List<SchedulerTaskData> getAllSchedulerTask();

    /**
     * Получение параметров всех задач планировщика
     *
     * @param pagingParams параметры пагиинации
     * @return Список задач {@link SchedulerTaskData}
     */
    PagingResult<TaskSearchResultItem> fetchAllSchedulerTasks(PagingParams pagingParams);

    /**
     * Изменение признака активности задач
     *
     * @param active признак активности
     * @param ids    идентификаторы задач
     */
    void setActiveSchedulerTask(boolean active, List<Long> ids);

    /**
     * Обновить дату последнего запуска задачи
     *
     * @param task объект {@link SchedulerTask} задачи
     */
    void updateTaskStartDate(SchedulerTask task);

    /**
     * Обновление параметров задачи планировщика
     *
     * @param taskData объект {@link SchedulerTaskData} задачи
     */
    void updateTask(SchedulerTaskData taskData);

    /**
     * Валидация расписания
     *
     * @param schedule строка расписания в формате cron
     * @return признак валидности
     */
    boolean validateSchedule(String schedule);
}
