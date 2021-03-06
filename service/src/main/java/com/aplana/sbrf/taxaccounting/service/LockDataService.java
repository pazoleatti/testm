package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;

import java.util.Collection;
import java.util.List;

/**
 * <h1>Сервис блокировок</h1>
 * <p>Порядок работы с блокировками:</p>
 * полная синхронизация даже в рамках одного пользователя:
 * <ol>
 * <li>Пытаемся установить блокировку на объект - метод "lockWait"</li>
 * <li>Выполняем операции над объектом</li>
 * <li>Если операция длительная, то для продления блокировки вызываем "extend"</li>
 * <li>После завершения работы над объектом вызываем метод "unlock"</li>
 * </ol>
 * обычное разграничение объектов (НФ, деклараций) между пользователями:
 * <ol>
 * <li>Пытаемся установить блокировку
 * <ol>
 * <li>Удалось установить блокировку
 * <ol>
 * <li>Выполняем операции над объектом</li>
 * <li>Если операция длительная, то периодически вызываем метод "extend"</li>
 * <li>После завершения работы над объектом вызываем метод "unlock"</li>
 * </ol>
 * </li>
 * <li>Не удалось установить блокировку
 * <ol>
 * <li>Отображаем пользователю информацию о блокировке - кто и до какого времени заблокировал</li>
 * </ol>
 * </li>
 * </ol>
 * </li>
 * </ol>
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 13:50
 */
@ScriptExposed
public interface LockDataService {

    /**
     * Устанавливает новую блокировку. Если блокировка успешно установилась, то возвращается null.
     * Если блокировка уже существовала, то возвращется информация по этой блокировке в виде объекта LockData
     *
     * @param key         код блокировки
     * @param userId      код установившего блокировку пользователя
     * @param description описание блокировки
     * @return информация о блокировке
     */
    LockData lock(String key, int userId, String description);

    /**
     * Устанавливает блокировки для выполнения асинхронной задачи над объектом
     *
     * @param key    код блокировки
     * @param userId код установившего блокировку пользователя
     * @return информация о блокировке
     */
    LockData lockAsync(String key, int userId);

    /**
     * Возвращает данные блокировки по ключу
     *
     * @param key ключ блокировки
     * @return данные блокировки. Если блокировки не существует - возвращается null
     */
    LockData findLock(String key);

    /**
     * Получает список всех блокировок в виде объектов предназначенных для отображения в GUI
     *
     * @param filter       ограничение по имени пользователя или ключу
     * @param pagingParams параметры пэйджинга
     * @param user         пользователь запрашивающий данные
     * @return все блокировки
     */
    PagingResult<LockDataDTO> findAllByFilter(String filter, PagingParams pagingParams, TAUser user);

    /**
     * Простое снятие блокировки по ключу.
     *
     * @param key уникальный ключ блокировки
     */
    void unlock(String key);

    /**
     * Снимает блокировку по ее идентификатору. Если блокировки не было, либо была установлена другим пользователем, то exception.
     *
     * @param key    код блокировки
     * @param userId код установившего блокировку пользователя
     * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если блокировка была установлена другим пользователем, либо блокировки не было в бд
     * @deprecated аналитика сведена к единому алгоритму, реализованному в {@link #unlock(String)}, проверки, если требуются, выполняем в вызывающих алгоритмах.
     */
    @Deprecated
    Boolean unlock(String key, int userId);

    /**
     * Снимает блокировку по ее идентификатору. Если блокировки не было, либо была установлена другим пользователем, то exception.
     *
     * @param key    код блокировки
     * @param userId код установившего блокировку пользователя
     * @param force  при force = true блокировка снимается принудительно, без вывода ошибки при отсутсвии блокировки
     * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если блокировка была установлена другим пользователем, либо блокировки не было в бд
     * @deprecated аналитика сведена к единому алгоритму, реализованному в {@link #unlock(String)}, проверки, если требуются, выполняем в вызывающих алгоритмах.
     */
    @Deprecated
    Boolean unlock(String key, int userId, boolean force);

    /**
     * Убрать все блокировки пользователя.
     *
     * TODO: Метод закомментирован в вызывающем коде controller/LogSystemLogoutHandler#logout()
     *
     * @param userInfo    пользователь
     * @param ignoreError признак игнорирования ошибок при снятии блокировок. Нужен при разлогинивании
     */
    @SuppressWarnings("unused")
    void unlockAll(TAUserInfo userInfo, boolean ignoreError);

    /**
     * Удаляет все указанные блокировки, но только если к ним не привязаны незавершенные асинхронные задачи.
     * Если хотя бы по одному ключу есть блокировка, ничего не делает, пишет ошибки в logger результата.
     *
     * @param lockKeys ключи блокировок
     */
    ActionResult unlockAllWithoutTasks(List<String> lockKeys, TAUser user);

    /**
     * Удаляет блокировки, созданные ранее "seconds" секунд назад. Предполагается, что данный метод
     * будет вызываться только из задачи планировщика
     *
     * @param seconds "срок годности" блокировки в секундах
     * @return количество удаленных блокировок
     */
    int unlockIfOlderThan(long seconds);

    /**
     * Удаляет все блокировки, связанные с асинхронной задачей
     *
     * @param taskId идентификатор задачи
     */
    void unlockAllByTask(long taskId);

    /**
     * Проверяет, установлена ли блокировка на указанном объекте
     *
     * @param key  код блокировки
     * @return блокировка установлена?
     */
    boolean lockExists(String key);

    /**
     * Связывает блокировку с асинхронной задачей
     *
     * @param lockKey ключ блокировки
     * @param taskId  идентификатор задачи
     */
    void bindTask(String lockKey, long taskId);

    /**
     * Получает список всех блокировок по части ключа
     *
     * @param keysBlocker - множество ключей
     * @return список существующих блокировок
     */
    List<LockDataDTO> fetchAllByKeyPrefixSet(Collection<String> keysBlocker);

    /**
     * Связывает несколько блокировок с асинхронной задачей
     *
     * @param keys   ключи блокировок
     * @param taskId идентификатор задачи
     */
    void bindTaskToMultiKeys(Collection<String> keys, long taskId);

    /**
     * Снять несколько блокировок
     *
     * @param keys список ключей блокировок
     */
    void unlockMultipleTasks(Collection<String> keys);

    /**
     * Получить идентификатор декларации из ключа блокировки
     *
     * @param lockKey ключ блокировки
     * @return идентикатор декларации
     */
    long getDeclarationIdByLockKey(String lockKey);
}
