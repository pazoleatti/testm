package com.aplana.sbrf.taxaccounting.core.api;

/**
 * Интерфейс для обновления статуса асинхронных задач
 * @author dloshkarev
 */
public interface LockStateLogger {
    void updateState(String state);
}
