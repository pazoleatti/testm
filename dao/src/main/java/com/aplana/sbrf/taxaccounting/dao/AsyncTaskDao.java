package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskState;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;

import java.util.List;
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
    AsyncTaskTypeData getTaskTypeData(long asyncTaskTypeId);

    /**
     * Сохраняет в БД информацию о новой асинхронной задаче
     * @param taskTypeId тип задачи
     * @param userId идентификатор пользователя, от имени которого была запущена задача
     * @param description описание задачи
     * @param queue тип очереди задачи - короткая или длинная
     * @param priorityNode приоритетный узел, на который будет назначена задача. Если = null, то назначается на любой узел
     * @param params параметры для выполнения задачи. Сериализуются и сохраняются в БД
     * @return идентификатор задачи
     */
    AsyncTaskData addTask(long taskTypeId, int userId, String description, AsyncQueue queue, String priorityNode, Map<String, Object> params);

    /**
     * Резервирует задачу с минимальной датой создания, которая не назначена ни одному из узлов либо, либо обработка которой превысила указанный таймаут и значит обрабатывающий ее узел упал
     * @param node узел, для которого будет зарезервирована задача
     * @param timeout таймаут на выполнение задач (часов)
     * @param queue тип очереди из которой будет выбрана задача: коротких или длинных задач
     * @param maxTasksPerNode максимальное количество задач, которое параллельно может обрабатываться в этой очереди на одном узле
     * @return количество зарезервированных задач
     */
    int lockTask(String node, int timeout, AsyncQueue queue, int maxTasksPerNode);

    /**
     * Возвращает данные задачи по ее идентификатору
     * @param taskId идентификатор задачи
     * @return данные конкретной задачи либо null, если подходящая задача не найдена
     */
    AsyncTaskData getTaskData(long taskId);

    /**
     * Возвращает данные задачи по ее идентификатору. Не извлекает сериализованные параметры
     * @param taskId идентификатор задачи
     * @return данные конкретной задачи либо null, если подходящая задача не найдена
     */
    AsyncTaskData getLightTaskData(long taskId);

    /**
     * Возвращает последнюю зарезервированную задачу для указанного узла
     * @param node узел
     * @param queue тип очереди из которой будет выбрана задача: коротких или длинных задач
     * @return данные конкретной задачи либо null, если подходящая задача не найдена либо указанный узел уже занят какой то задачей
     */
    AsyncTaskData getLockedTask(String node, AsyncQueue queue);

    /**
     * Обновляет статус выполнения асинхронной задачи
     * @param taskId идентификатор задачи
     * @param state новый статус
     */
    void updateState(long taskId, AsyncTaskState state);

    /**
     * Удаляет задачу
     * @param taskId идентификатор задачи
     */
    void finishTask(long taskId);

    /**
     * Отменяет задачу (проставляет статус CANCELLED)
     * @param taskId идентификатор задачи
     */
    void cancelTask(long taskId);

    /**
     * Освобождает задачу от резервирования узлом
     * @param taskId идентификатор задачи
     */
    void releaseTask(long taskId);

    /**
     * Проверяет, активна ли задача с указанным идентификатором
     * @param taskId идентификатор задачи
     * @return задача активна?
     */
    boolean isTaskActive(long taskId);

    /**
     * Возвращает список идентификаторов пользователей, которые ожидают выполнения указанной задачи
     * @param taskId идентификатор задачи
     * @return список идентификаторов пользователей
     */
    List<Integer> getUsersWaitingForTask(long taskId);

    /**
     * Добавляет пользователя в список ожидающих выполнения указанной задачи
     * @param taskId идентификатор задачи
     * @param userId идентификатор пользователя
     */
    void addUserWaitingForTask(long taskId, int userId);
}
