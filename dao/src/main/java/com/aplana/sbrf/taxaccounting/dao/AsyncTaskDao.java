package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;
import java.util.Map;


/**
 * DAO-Интерфейс для работы с асинхронными задачами
 */
public interface AsyncTaskDao {

    /**
     * Получение данных типа асинхронной задачи по ее id
     *
     * @param asyncTaskTypeId тип задачи
     * @return данные задачи
     */
    AsyncTaskTypeData getTaskTypeData(long asyncTaskTypeId);

    /**
     * Сохраняет в БД информацию о новой асинхронной задаче
     *
     * @param taskTypeId   тип задачи
     * @param userId       идентификатор пользователя, от имени которого была запущена задача
     * @param description  описание задачи
     * @param queue        тип очереди задачи - короткая или длинная
     * @param priorityNode приоритетный узел, на который будет назначена задача. Если = null, то назначается на любой узел
     * @param params       параметры для выполнения задачи. Сериализуются и сохраняются в БД
     * @return идентификатор задачи
     */
    AsyncTaskData addTask(long taskTypeId, int userId, String description, AsyncQueue queue, String priorityNode, Map<String, Object> params);

    /**
     * Резервирует задачу с минимальной датой создания, которая не назначена ни одному из узлов либо, либо обработка которой превысила указанный таймаут и значит обрабатывающий ее узел упал
     *
     * @param node            узел, для которого будет зарезервирована задача
     * @param priorityNode    узел, для которого будут принудительно отбираться задачи. Используется для dev-moda - отбираются задачи только с этим узлом
     * @param timeout         таймаут на выполнение задач (часов)
     * @param queue           тип очереди из которой будет выбрана задача: коротких или длинных задач
     * @param maxTasksPerNode максимальное количество задач, которое параллельно может обрабатываться в этой очереди на одном узле
     * @return количество зарезервированных задач
     */
    int lockTask(String node, String priorityNode, int timeout, AsyncQueue queue, int maxTasksPerNode);

    /**
     * Возвращает данные задачи по ее идентификатору
     *
     * @param taskId идентификатор задачи
     * @return данные конкретной задачи либо null, если подходящая задача не найдена
     */
    AsyncTaskData getTaskData(long taskId);

    /**
     * Возвращает данные задачи по ее идентификатору. Не извлекает сериализованные параметры
     *
     * @param taskId идентификатор задачи
     * @return данные конкретной задачи либо null, если подходящая задача не найдена
     */
    AsyncTaskData getLightTaskData(long taskId);

    /**
     * Возвращает последнюю зарезервированную задачу для указанного узла
     *
     * @param node  узел
     * @param queue тип очереди из которой будет выбрана задача: коротких или длинных задач
     * @return данные конкретной задачи либо null, если подходящая задача не найдена либо указанный узел уже занят какой то задачей
     */
    AsyncTaskData getLockedTask(String node, AsyncQueue queue);

    /**
     * Обновляет статус выполнения асинхронной задачи
     *
     * @param taskId идентификатор задачи
     * @param state  новый статус
     */
    void updateState(long taskId, AsyncTaskState state);

    /**
     * Удаляет задачу
     *
     * @param taskId идентификатор задачи
     */
    void finishTask(long taskId);

    /**
     * Отменяет задачу (проставляет статус CANCELLED)
     *
     * @param taskId идентификатор задачи
     */
    void cancelTask(long taskId);

    /**
     * Проверяет, активна ли задача с указанным идентификатором
     *
     * @param taskId идентификатор задачи
     * @return задача активна?
     */
    boolean isTaskActive(long taskId);

    /**
     * Возвращает список идентификаторов пользователей, которые ожидают выполнения указанной задачи
     *
     * @param taskId идентификатор задачи
     * @return список идентификаторов пользователей
     */
    List<Integer> getUsersWaitingForTask(long taskId);

    /**
     * Добавляет пользователя в список ожидающих выполнения указанной задачи
     *
     * @param taskId идентификатор задачи
     * @param userId идентификатор пользователя
     */
    void addUserWaitingForTask(long taskId, int userId);

    /**
     * Получает список асинхронных задач + пейджинг. Используется на форме списка асинхронных задач.
     *
     * @param filter       ограничение по имени пользователя или ключу. Необязательный параметр. Может быть null
     * @param pagingParams параметры пэйджинга. Обязательный параметр
     * @return все блокировки
     */
    PagingResult<AsyncTaskDTO> getTasks(String filter, PagingParams pagingParams);

    /**
     * Очищает поле ASYNC_TASK.NODE и проставляет статус "В очереди на выполнение" для всех задач, выполняющихя на узле
     * @param node узел
     */
    void releaseNodeTasks(String node);

    /**
     * Получает список идентификаторов задач, привязанных к указанному узлу. Используется только в dev-моде
     * @param priorityNode узел, назначенный для выполнения
     * @return список идентификаторов
     */
    List<Long> getTasksByPriorityNode(String priorityNode);

    /**
     * Проверяет существование задачи по ее id
     * @param taskId идентификатор задачи
     * @return задача существует?
     */
    boolean isTaskExists(long taskId);

    /**
     * Получение списка типов асинхронных задач
     *
     * @param pagingParams параметры пагинации
     * @return страница {@link PagingResult} с данными {@link AsyncTaskTypeData}
     */
    PagingResult<AsyncTaskTypeData> fetchAllAsyncTaskTypeData(PagingParams pagingParams);
}
