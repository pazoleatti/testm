package com.aplana.sbrf.taxaccounting.async.manager;

import java.util.Collection;

/**
 * Менеджер для остановки запущенных асинхронных задач
 * @author dloshkarev
 */
public interface AsyncInterruptionManager {

    /**
     * Добавляет задачу в список
     * @param key ключ асинхронной задачи
     * @param task асинхронная задача, обернутая в отдельный поток
     */
    void addTask(String key, Thread task);

    /**
     * Останавливает выполнение задач с указанными ключами блокировки
     * @param keys ключи блокировки
     */
    void interruptAll(Collection<String> keys);
}
