package com.aplana.sbrf.taxaccounting.async.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Local(AsyncInterruptionManagerLocal.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AsyncInterruptionManagerSingleton implements AsyncInterruptionManager {
    private final Log log = LogFactory.getLog(getClass());

    private ConcurrentHashMap<String, Thread> tasks = new ConcurrentHashMap<String, Thread>();

    @Override
    public void addTask(String key, Thread task) {
        log.info(String.format("Для управления выполнением добавлена асинхронная задача с ключом %s", key));
        tasks.put(key, task);
    }

    @Override
    public void interruptAsyncTask(String key) {
        log.info(String.format("Останавливается асинхронная задача с ключом %s", key));
        tasks.get(key).interrupt();
        tasks.remove(key);
    }

    @Override
    public void interruptAll() {
        log.info(String.format("Запущена остановка всех асинхронных задач"));
        for (Thread task : tasks.values()) {
            task.interrupt();
        }
        tasks.clear();
    }
}
