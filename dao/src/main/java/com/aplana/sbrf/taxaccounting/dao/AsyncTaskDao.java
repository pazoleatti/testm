package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;

import java.util.Map;


/**
 * DAO-Интерфейс для работы с асинхронными задачами
 */
public interface AsyncTaskDao {

    /**
     * Получение данных типа асинхронной задачи по ее id
     * @param asyncTaskTypeId тип задачи
     * @return данные задачи
     */
    AsyncTaskTypeData getTaskData(Long asyncTaskTypeId);

    /**
     * Сохраняет в БД информацию о новой асинхронной задаче
     * @param taskTypeId тип задачи
     * @param balancingVariant тип очереди задачи - короткая или длинная
     * @param params параметры для выполнения задачи. Сериализуются и сохраняются в БД
     */
    void addTask(long taskTypeId, BalancingVariants balancingVariant, Map<String, Object> params);

    /**
     * Резервирует задачу с минимальной датой создания, которая не назначена ни одному из узлов либо, либо обработка которой превысила указанный таймаут и значит обрабатывающий ее узел упал
     * @param node узел, для которого будет зарезервирована задача
     * @param timeout таймаут на выполнение задач (часов)
     * @param balancingVariants тип очереди из которой будет выбрана задача: коротких или длинных задач
     * @param maxTasksPerNode максимальное количество задач, которое параллельно может обрабатываться в этой очереди на одном узле
     * @return количество зарезервированных задач
     */
    int lockTask(String node, int timeout, BalancingVariants balancingVariants, int maxTasksPerNode);

    /**
     * Возвращает последнюю зарезервированную задачу для указанного узла
     * @param node узел
     * @param balancingVariants тип очереди из которой будет выбрана задача: коротких или длинных задач
     * @return данные конкретной задачи либо null, если подходящая задача не найдена либо указанный узел уже занят какой то задачей
     */
    AsyncTaskData getLockedTask(String node, BalancingVariants balancingVariants);

    /**
     * Удаляет задачу с указанным id
     * @param taskId id задачи
     */
    void finishTask(Long taskId);

    /**
     * Освобождает задачу от резервирования узлом
     * @param taskId идентификатор задачи
     */
    void releaseTask(long taskId);
}
