package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
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

    /**
     * Возвращает первую в очереди задачу (с минимальной датой создания), которая не назначена ни одному из узлов либо, либо обработка которой превысила указанный таймаут и значит обрабатывающий ее узел упал
     * Одновременно резервирует ее для указанного узла, а также проверяет не занят ли указанный узел уже какой то задачей
     * @param node узел, для которого будет зарезервирована задача
     * @param timeout таймаут на выполнение задач (часов)
     * @param balancingVariants тип очереди из которой будет выбрана задача: коротких или длинных задач
     * @param maxTasksPerNode максимальное количество задач, которое параллельно может обрабатываться в этой очереди на одном узле
     * @return данные конкретной задачи либо null, если подходящая задача не найдена либо указанный узел уже занят какой то задачей
     */
    AsyncTaskData reserveTask(String node, int timeout, BalancingVariants balancingVariants, int maxTasksPerNode);
}
