package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;

import java.util.List;

public interface SchedulerTaskService {
    /**
     * Возвращяет данные по id задачи
     *
     * @param task объект {@link SchedulerTask} задачи
     * @return объект {@link SchedulerTaskData} задачи
     */
    SchedulerTaskData fetchOne(SchedulerTask task);

    /**
     * Возвращяет данные по id задачи
     *
     * @param taskId id задачи
     * @return объект {@link SchedulerTaskData} задачи
     */
    SchedulerTaskData fetchOne(Long taskId);


    /**
     * Возвращяет все задачи планировщика
     *
     * @return список задач {@link SchedulerTaskData}
     */
    List<SchedulerTaskData> fetchAll();

    /**
     * Возвращяет страницу параметров всех задач планировщика
     *
     * @param pagingParams параметры пагиинации
     * @return Страница задач {@link SchedulerTaskData}
     */
    PagingResult<TaskSearchResultItem> fetchAllByPaging(PagingParams pagingParams);

    /**
     * Изменяет признак активности задач
     *
     * @param active признак активности
     * @param ids    идентификаторы задач
     */
    void updateActiveByIds(boolean active, List<Long> ids);

    /**
     * Изменяет дату последнего запуска задачи
     *
     * @param task объект {@link SchedulerTask} задачи
     */
    void updateStartDate(SchedulerTask task);

    /**
     * Изменяет параметры задачи планировщика
     *
     * @param taskData объект {@link SchedulerTaskData} задачи
     */
    String update(SchedulerTaskData taskData);

    /**
     * Валидация расписания
     *
     * @param schedule строка расписания в формате cron
     * @return признак валидности
     */
    boolean validateSchedule(String schedule);
}
