package com.aplana.sbrf.taxaccounting.core.api;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskState;

/**
 * Интерфейс для обновления статуса асинхронных задач
 * @author dloshkarev
 */
public interface LockStateLogger {
    void updateState(AsyncTaskState state);
}
