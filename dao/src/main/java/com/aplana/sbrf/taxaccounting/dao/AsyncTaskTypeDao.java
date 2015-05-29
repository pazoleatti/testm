package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;


/**
 * DAO-Интерфейс для работы с типами асинхронных задач
 */
public interface AsyncTaskTypeDao {

    /**
     * Получение данных по id задачи
     * @param asyncTaskTypeId
     * @return
     */
    AsyncTaskTypeData get(Long asyncTaskTypeId);
}
