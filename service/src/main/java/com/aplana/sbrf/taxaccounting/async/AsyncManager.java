package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для асинхронного выполнения задач
 * @author dloshkarev
 */
public interface AsyncManager {

    /**
     * Возвращает данные задачи по ее идентификатору. Не извлекает сериализованные параметры
     * @param taskId идентификатор задачи
     * @return данные конкретной задачи либо null, если подходящая задача не найдена
     */
    AsyncTaskData getLightTaskData(long taskId);

    /**
     * Добавляет задачу в очередь выполнения
     * @param lockKey блокировка, к которой будет привязана задача
     * @param taskType тип задачи
     * @param user пользователь, от имени которого была запущена задача
     * @param queue очередь, в которую надо поместить задачу
     * @param params кастомные параметры задачи
     * @return данные задачи, которая была поставлена в очередь, либо null, если она поставлена не была
     */
    AsyncTaskData executeTask(String lockKey, AsyncTaskType taskType, TAUserInfo user, AsyncQueue queue, Map<String, Object> params) throws AsyncTaskException;
    AsyncTaskData executeTask(String lockKey, AsyncTaskType taskType, TAUserInfo user, AsyncQueue queue) throws AsyncTaskException;

    /**
     * Очередь будет определена на основе расчета лимитирующих параметров
     */
    AsyncTaskData executeTask(String lockKey, AsyncTaskType taskType, TAUserInfo user) throws AsyncTaskException;
    AsyncTaskData executeTask(String lockKey, AsyncTaskType taskType, TAUserInfo user, Map<String, Object> params) throws AsyncTaskException;

    /**
     * Выполнение асинхронной задачи по постановке http://conf.aplana.com/pages/viewpage.action?pageId=19663772
     * @param cancelConfirmed признак того, что пользователь подтвердил отмену существующих задач
     * @param handler обработчик постановки задачи в очередь
     * @return данные задачи, которая была поставлена в очередь, либо null, если она поставлена не была
     */
    AsyncTaskData executeTask(String lockKey, AsyncTaskType taskType, TAUserInfo user, Map<String, Object> params, Logger logger, boolean cancelConfirmed, AbstractStartupAsyncTaskHandler handler);

    /**
     * Выполняет попытку запуска асинхронной задачи, если не удалось - возвращает сообщение для диалога
     * Возвращает пару (lock, restartMsg)
     * если lock == true, то существует блокировка и нужно вызвать диалог перезапуска с текстом restartMsg
     * если lock == false, добавили пользователя в очередь ожидания, выходим из сценария
     * иначе, нет блокировки/удалили блокировку с таким ключом, продолжаем выполнение сценария
     * @param lockKey блокировка, к которой будет привязана задача
     * @param user пользователь, который пытается перезапустить задачу
     * @param force признак принудительного перезапуска задачи (без диалога подтверждения)
     * @param logger логгер с сообщениями из предыдущих операций
     * @return пара (признак блокировки, сообщение)
     */
    Pair<Boolean, String> restartTask(String lockKey, TAUserInfo user, boolean force, Logger logger);

    /**
     * Останавливает выполнение задач с указанным идентификатором, удаляет задачу и
     * отправляет оповещения ожидающим завершения задачи пользователям
     * @param taskId идентификатор задачи, которая будет перезапущена
     * @param user пользователь, который пытается перезапустить задачу
     * @param cause причина остановки задачи
     */
    void interruptTask(long taskId, TAUserInfo user, TaskInterruptCause cause);
    void interruptTask(AsyncTaskData taskData, TAUserInfo user, TaskInterruptCause cause);
    void interruptTask(String lockKey, TAUserInfo user, TaskInterruptCause cause);

    /**
     * Завершает задачу. Удаляет ее из бд, а также удаляет связанные с ней блокировки
     * @param taskId идентификатор задачи
     */
    void finishTask(long taskId);

    /**
     * Останавливает выполнение задач с указанными идентификаторами, удаляет задачи и
     * отправляет оповещения ожидающим завершения задачи пользователям
     * @param taskIds идентификаторы задач, которые будут перезапущены
     * @param user пользователь, который пытается перезапустить задачу
     * @param cause причина остановки задачи
     */
    void interruptAllTasks(List<Long> taskIds, TAUserInfo user, TaskInterruptCause cause);

    /**
     * Обновляет статус выполнения асинхронной задачи. Выполняется в отдельной транзакции
     * @param taskId идентификатор задачи
     * @param state новый статус
     */
    void updateState(long taskId, AsyncTaskState state);

    /**
     * Получает бин-обработчик задачи по его типу
     * @param taskTypeId тип задачи
     * @return бин-обработчик
     * @throws AsyncTaskException может быть выброшен в случае, если в БД не найден соответствующий тип задачи, некорректного имени бина-обработчика или других непредвиденных ошибках
     */
    AsyncTask getAsyncTaskBean(long taskTypeId) throws AsyncTaskException;

    /**
     * Возвращает первую в очереди задачу (с минимальной датой создания), которая не назначена ни одному из узлов либо, либо обработка которой превысила указанный таймаут и значит обрабатывающий ее узел упал
     * Одновременно резервирует ее для указанного узла, а также проверяет не занят ли указанный узел уже какой то задачей
     * @param node узел, для которого будет зарезервирована задача
     * @param priorityNode узел, для которого будут принудительно отбираться задачи. Используется для dev-moda - отбираются задачи только с этим узлом
     * @param timeout таймаут на выполнение задач (часов)
     * @param balancingVariants тип очереди из которой будет выбрана задача: коротких или длинных задач
     * @param maxTasksPerNode максимальное количество задач, которое параллельно может обрабатываться в этой очереди на одном узле
     * @return данные конкретной задачи либо null, если подходящая задача не найдена либо указанный узел уже занят какой то задачей
     */
    AsyncTaskData reserveTask(String node, String priorityNode, int timeout, AsyncQueue balancingVariants, int maxTasksPerNode);

    /**
     * Добавляет пользователя в список ожидающих выполнения задачи
     * @param taskId идентификатор задачи
     * @param userId идентификатор пользователя
     */
    void addUserWaitingForTask(long taskId, int userId);

    /**
     * Возвращает список идентификаторов пользователей, которые ожидают разблокировки указанного объекта
     * @param taskId идентификатор задачи
     * @return список идентификаторов пользователей
     */
    List<Integer> getUsersWaitingForTask(long taskId);

    /**
     * Получает список асинхронных задач + пейджинг. Используется на форме списка асинхронных задач.
     *
     * @param filter       ограничение по имени пользователя или ключу. Необязательный параметр. Может быть null
     * @param pagingParams параметры пэйджинга. Обязательный параметр
     * @return все блокировки
     */
    PagingResult<AsyncTaskDTO> getTasks(String filter, PagingParams pagingParams);
}
