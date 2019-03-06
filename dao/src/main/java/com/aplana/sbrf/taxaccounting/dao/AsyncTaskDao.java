package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;
import java.util.Map;


/**
 * DAO-Интерфейс для работы с асинхронными задачами
 */
public interface AsyncTaskDao {

    /**
     * Возвращает все асинхронные задачи
     *
     * @return список асинхронных задач
     */
    List<AsyncTaskData> findAll();

    /**
     * Постраничная выборка асинхронных задач с учётом фильтрации.
     *
     * @param filter       ограничение по имени пользователя или ключу. Необязательный параметр. Может быть null
     * @param pagingParams параметры пэйджинга. Обязательный параметр
     * @return страница {@link AsyncTaskDTO}
     */
    PagingResult<AsyncTaskDTO> findAll(String filter, PagingParams pagingParams);

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
    AsyncTaskData create(long taskTypeId, int userId, String description, AsyncQueue queue, String priorityNode,
                         Map<String, Object> params);

    /**
     * Резервирует задачу с минимальной датой создания, которая не назначена ни одному из узлов,
     * либо обработка которой превысила указанный таймаут и значит обрабатывающий ее узел упал, и возвращяет её
     *
     * @param node            узел, для которого будет зарезервирована задача
     * @param priorityNode    узел, для которого будут принудительно отбираться задачи. Используется для dev-moda - отбираются задачи только с этим узлом
     * @param timeoutHours    таймаут на выполнение задач (часов)
     * @param queue           тип очереди из которой будет выбрана задача: коротких или длинных задач
     * @param maxTasksPerNode максимальное количество задач, которое параллельно может обрабатываться в этой очереди на одном узле
     * @return зарезервированная задача либо null
     */
    Long reserveTask(String node, String priorityNode, int timeoutHours, AsyncQueue queue, int maxTasksPerNode);

    /**
     * Возвращает данные задачи по ее идентификатору
     *
     * @param id идентификатор задачи
     * @return данные конкретной задачи либо null, если подходящая задача не найдена
     */
    AsyncTaskData findById(long id);

    /**
     * Возвращает данные задачи по ее идентификатору. Не извлекает сериализованные параметры
     *
     * @param id идентификатор задачи
     * @return данные конкретной задачи либо null, если подходящая задача не найдена
     */
    AsyncTaskData findByIdLight(long id);

    /**
     * Обновляет статус выполнения асинхронной задачи
     *
     * @param id    идентификатор задачи
     * @param state новый статус
     */
    void updateState(long id, AsyncTaskState state);

    /**
     * Удаляет задачу
     *
     * @param id идентификатор задачи
     */
    void delete(long id);

    /**
     * Возвращает список идентификаторов пользователей, которые ожидают выполнения указанной задачи
     *
     * @param id идентификатор задачи
     * @return список идентификаторов пользователей
     */
    List<Integer> findUserIdsWaitingForTask(long id);

    /**
     * Добавляет пользователя в список ожидающих выполнения указанной задачи
     *
     * @param asyncTaskId идентификатор задачи
     * @param userId      идентификатор пользователя
     */
    void addUserWaitingForTask(long asyncTaskId, int userId);

    /**
     * Для всех задач с указанным узлом (Node) проставляет статус AsyncTaskState.IN_QUEUE ("В очереди на выполнение") и затем очищает поле Node.
     *
     * @param node узел
     */
    void releaseNodeTasks(String node);

    /**
     * Получает список идентификаторов задач, привязанных к указанному узлу. Используется только в dev-моде
     *
     * @param priorityNode узел, назначенный для выполнения
     * @return список идентификаторов задач
     */
    List<Long> findAllByPriorityNode(String priorityNode);

    /**
     * Проверяет существование задачи по ее id
     *
     * @param id идентификатор задачи
     * @return true если задача существует
     */
    boolean isTaskExists(long id);

    /**
     * Проверяет, активна ли задача с указанным идентификатором
     *
     * @param id идентификатор задачи
     * @return true если задача существует и имеет статус отличный от AsyncTaskState.CANCELLED
     */
    boolean isTaskActive(long id);

    /**
     * Находит задачу в списке по ключу блокировки
     * @param lockKey   строка ключа блокировки
     * @return данные конкретной задачи либо null, если подходящая задача не найдена
     */
    AsyncTaskData findAsyncTaskByLockKey(String lockKey);
}
