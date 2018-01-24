package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;

import java.util.List;

/**
 * DAO-Интерфейс для работы с планировщиком
 */
public interface SchedulerTaskDao {

    /**
     * Возвращяет данные по id задачи
     *
     * @param taskId идентификатор задачи
     * @return объект {@link SchedulerTaskData} или null, если не найден
     */
    SchedulerTaskData fetchOne(Long taskId);

    /**
     * Возвращяет все задачи планировщика
     *
     * @return Список задач {@link SchedulerTaskData}
     */
    List<SchedulerTaskData> fetchAll();

    /**
     * Возвращяет страницу параметров всех задач планировщика
     *
     * @param pagingParams параметры пагиинации
     * @return Страница задач {@link SchedulerTaskData}
     */
    PagingResult<SchedulerTaskData> fetchAllByPaging(PagingParams pagingParams);

    /**
     * Изменяет параметры задачи планировщика
     *
     * @param taskData объект {@link SchedulerTaskData} задачи
     */
    void update(SchedulerTaskData taskData);

    /**
     * Изменяет дату последнего запуска задачи
     *
     * @param taskId идентификатор задачи
     */
    void updateStartDate(long taskId);

    /**
     * Изменяет признак активности задач
     *
     * @param active признак активности
     * @param ids    список идентификаторов задач
     */
    void updateActiveByIds(boolean active, List<Long> ids);
}
