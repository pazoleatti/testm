package com.aplana.sbrf.taxaccounting.async.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Local(AsyncInterruptionManagerLocal.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AsyncInterruptionManagerSingleton implements AsyncInterruptionManager {

    private static final Log log = LogFactory.getLog(AsyncInterruptionManagerSingleton.class);

    private ConcurrentHashMap<String, Thread> tasks = new ConcurrentHashMap<String, Thread>();

    @Override
    public void addTask(String key, Thread task) {
        log.info(String.format("Для управления выполнением добавлена асинхронная задача с ключом %s", key));
        tasks.put(key, task);
    }

    @Override
    public void interruptAll(Collection<String> keys) {
        synchronized(AsyncInterruptionManagerSingleton.class) {
            try {
                for (String key : keys) {
                    log.info(String.format("Останавливается асинхронная задача с ключом %s", key));
                    Thread task = tasks.get(key);
                    if (task != null) {
                        //TODO: так делать нехорошо, но иначе никак. Видел ошибку The exception is: java.sql.SQLException: OALL8 находится в противоречивом состоянии но на работу приложения это вроде не повлияло
                        task.stop();
                        tasks.remove(key);
                    }
                }
            } catch (Exception e) {
                log.error("Не удалось отменить задачу", e);
                //Игнорируем ошибки
            }
        }
    }
}