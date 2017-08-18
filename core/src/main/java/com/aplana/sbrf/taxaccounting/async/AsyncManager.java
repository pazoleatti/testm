package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;

import java.util.Map;

/**
 * Интерфейс для асинхронного выполнения задач
 * @author dloshkarev
 */
public interface AsyncManager {
    /**
     * Получает бин-обработчик задачи по его типу
     * @param taskTypeId тип задачи
     * @return бин-обработчик
     * @throws AsyncTaskException может быть выброшен в случае, если в БД не найден соответствующий тип задачи, некорректного имени бина-обработчика или других непредвиденных ошибках
     */
    AsyncTask getAsyncTaskBean(Long taskTypeId) throws AsyncTaskException;

    /**
     * Добавляет задачу в очередь выполнения
     * @param taskTypeId идентификатор типа задачи, которому соответствует jndi-класса обработчика. Хранится в бд.
     * @param params параметры задачи. Предполагается, что вызывающая часть и код-исполнитель знают друг о друге и передаваемых параметрах.
     */
    void executeAsync(long taskTypeId, Map<String, Object> params, BalancingVariants balancingVariant) throws AsyncTaskException;

    /**
     * Проверки перед постановкой задачи в очередь, определение очереди
     * @param taskTypeId
     * @param params
     * @return
     * @throws AsyncTaskException
     */
    BalancingVariants checkCreate(long taskTypeId, Map<String, Object> params) throws AsyncTaskException;
}
