package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;

/**
 * DAO-Интерфейс для работы с типами асинхронных задач
 */
public interface AsyncTaskTypeDao {

    /**
     * Возвращяет тип асинхронной задачи по ее id
     *
     * @param id ид типа задачи
     * @return данные задачи
     */
    AsyncTaskTypeData findById(long id);

    /**
     * Возвращяет страницу типов асинхронных задач
     *
     * @param pagingParams параметры пагинации
     * @return страница {@link PagingResult} с данными {@link AsyncTaskTypeData}
     */
    PagingResult<AsyncTaskTypeData> findAll(PagingParams pagingParams);

    /**
     * Изменяет ограничения у типов асинхронных задач
     *
     * @param id              ид типа задачи, у которого меняются параметры
     * @param shortQueueLimit ограничение на выполнение задачи в очереди быстрых задач
     * @param taskLimit       ограничение на выполнение задачи
     */
    void updateLimits(long id, Long shortQueueLimit, Long taskLimit);
}
