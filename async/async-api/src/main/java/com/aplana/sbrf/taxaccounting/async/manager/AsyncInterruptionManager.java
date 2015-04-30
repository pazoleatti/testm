package com.aplana.sbrf.taxaccounting.async.manager;

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
     * Останавливает выполнение задачи с указанным ключом блокировки
     * @param key ключ блокировки
     */
    void interruptAsyncTask(String key);

    /**
     * Останавливает все задачи из списока
     */
    void interruptAll();
}
