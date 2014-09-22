package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

import java.util.Map;

/**
 * Абстрактная реализация асинхронной задачи.
 * В ней реализована общая часть логики взаимодействия с блокировками объектов, для которых выполняется бизнес-логика конкретных задач
 * @author dloshkarev
 */
public abstract class AbstractAsyncTask implements AsyncTask {
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    private LockDataService lockService;

    /**
     * Выполнение бизнес логики задачи
     * @param params параметры
     */
    protected abstract void executeBusinessLogic(Map<String, Object> params);

    /**
     * Возвращает название задачи. Используется при выводе ошибок.
     * @return название задачи
     */
    protected abstract String getAsyncTaskName();

    @Override
    public void execute(Map<String, Object> params) {
        String lock = (String) params.get(LOCKED_OBJECT.name());
        int userId = (Integer) params.get(USER_ID.name());
        if (lockService.checkLock(lock)) {
            //Если блокировка на объект задачи все еще существует, значит на нем можно выполнять бизнес-логику
            try {
                executeBusinessLogic(params);
            } catch (Exception e) {
                lockService.unlock(lock, userId);
                throw new RuntimeException("Не удалось выполнить задачу \"" + getAsyncTaskName() + "\". Выполняется откат транзакции. Произошла ошибка: " + e.getMessage(), e);
            }
            if (!lockService.checkLock(lock)) {
                //Если после выполнения бизнес логики, оказывается, что блокировки уже нет
                //Значит результаты нам уже не нужны - откатываем транзакцию и все изменения
                throw new RuntimeException("Результат выполнения задачи \"" + getAsyncTaskName() + "\" больше не актуален. Выполняется откат транзакции");
            }
            lockService.unlock(lock, userId);
        }
    }
}
