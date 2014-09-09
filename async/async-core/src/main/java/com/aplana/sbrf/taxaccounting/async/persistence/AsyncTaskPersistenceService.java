package com.aplana.sbrf.taxaccounting.async.persistence;

import com.aplana.sbrf.taxaccounting.async.entity.AsyncTaskTypeEntity;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskPersistenceException;

/**
 * Слой для работы с бд
 * @author dloshkarev
 */
public interface AsyncTaskPersistenceService {
    /**
     * Получает данные о типе задачи, по его идентификатору
     * @param taskTypeId идентификатор типа задачи
     * @return данные о типе задачи
     * @throws com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskPersistenceException
     */
    AsyncTaskTypeEntity getTaskTypeById(Long taskTypeId) throws AsyncTaskPersistenceException;
}
