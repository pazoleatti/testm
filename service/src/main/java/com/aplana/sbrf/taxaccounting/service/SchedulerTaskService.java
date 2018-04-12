package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;

import java.util.List;

/**
 * Сервис для работы с планировщиком задач
 */
public interface SchedulerTaskService {

    /**
     * Получени информации о задаче планировщика по объекту задачи
     *
     * @param task объект {@link SchedulerTask} задачи
     * @return объект {@link SchedulerTaskData} задачи
     */
    SchedulerTaskData fetchOne(SchedulerTask task);

    /**
     * Получение информации о задаче по id задачи
     *
     * @param taskId id задачи
     * @return объект {@link SchedulerTaskData} задачи
     */
    SchedulerTaskData fetchOne(Long taskId);


    /**
     * Получение списка всех задач планировщика
     *
     * @return список задач {@link SchedulerTaskData}
     */
    List<SchedulerTaskData> fetchAll();

    /**
     * Получение страницы параметров задач планировщика
     *
     * @param pagingParams параметры пагиинации
     * @return Страница задач {@link SchedulerTaskData}
     */
    PagingResult<TaskSearchResultItem> fetchAllByPaging(PagingParams pagingParams);

    /**
     * Обновление признака активности задач
     *
     * @param active признак активности
     * @param ids    идентификаторы задач
     */
    void updateActiveByIds(boolean active, List<Long> ids);

    /**
     * Обновление даты последнего запуска задачи
     *
     * @param task объект {@link SchedulerTask} задачи
     */
    void updateStartDate(SchedulerTask task);

    /**
     * Обновление параметров задачи планировщика
     *
     * @param taskData объект {@link SchedulerTaskData} задачи
     * @return сообщение об ошибки или null, если обновление прошло успешно
     */
    void update(SchedulerTaskData taskData);

    /**
     * Валидация крон выражения на соответствие формату
     *
     * @param schedule строка расписания в формате cron
     * @return признак валидности
     */
    boolean validateScheduleCronString(String schedule);
}
