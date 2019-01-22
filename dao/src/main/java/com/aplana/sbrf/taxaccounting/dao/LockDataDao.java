package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockDataDTO;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface LockDataDao extends PermissionDao {

    /**
     * Возвращает информацию о блокировке
     *
     * @param key код блокировки
     * @return возвращает null, если блокировка по данному коду не найдена
     */
    LockData findByKey(String key);

    /**
     * Проверяет наличие блокировки по ключу.
     *
     * @param key ключ блокировки
     */
    boolean existsByKey(String key);

    /**
     * Проверяет наличие блокировки по ключу и пользователю.
     *
     * @param key    ключ блокировки
     * @param userId идентификатор пользователя
     */
    boolean existsByKeyAndUserId(String key, int userId);

    /**
     * Создает новую блокировку
     *
     * @param key         код блокировки
     * @param userId      код пользователя, установившего блокировку
     * @param description описание блокировки
     */
    void lock(String key, int userId, String description);

    /**
     * Создает новую блокировку без описания. Используется для создания блокировок для асинхронных задач для упрощения кода,
     * т.к в этом случае описание хранится в самой асинхронной задаче
     *
     * @param key    код блокировки
     * @param userId код пользователя, установившего блокировку
     */
    void lock(String key, int userId);

    /**
     * Удаление блокировки по её ключу. Если блокировка не существует, ничего не делаем.
     *
     * @param key ключ блокировки
     */
    void unlock(String key);

    /**
     * Старый метод снятия блокировки, выбрасывающий исключение в случае её отсутствия.
     *
     * @param key ключ блокировки
     * @throws com.aplana.sbrf.taxaccounting.model.exception.LockException если блокировки нет в БД
     * @deprecated используйте {@link #unlock(String)}
     */
    @Deprecated
    void unlockOld(String key);

    /**
     * Убрать все блокировки пользователя.
     *
     * @param userId      идентификатор пользователя
     * @param ignoreError признак игнорирования ошибок при снятии блокировок. Нужен при разлогинивании
     */
    void unlockAllByUserId(int userId, boolean ignoreError);

    /**
     * Удаляет все блокировки, связанные с асинхронной задачей
     *
     * @param taskId идентификатор задачи
     */
    void unlockAllByTaskId(long taskId);

    /**
     * Получает список всех блокировок с учетом фильтра + пейджинг. Используется на форме просмотра блокировок.
     *
     * @param filter       ограничение по имени пользователя или ключу. Необязательный параметр. Может быть null
     * @param pagingParams параметры пэйджинга. Обязательный параметр
     * @param user         пользователь запрашивающий данные
     * @return все блокировки
     */
    PagingResult<LockDataDTO> getLocks(String filter, PagingParams pagingParams, TAUser user);

    /**
     * Возвращяет список истекших блокировок
     *
     * @param seconds "срок годности" блокировки в секундах
     * @return список истекший блокировок
     */
    List<String> getLockIfOlderThan(long seconds);

    /**
     * Связывает блокировку с асинхронной задачей
     *
     * @param lockKey ключ блокировки
     * @param taskId  идентификатор задачи
     */
    void bindTask(String lockKey, long taskId);

    /**
     * Получает список всех блокировок по ключам
     *
     * @param keysBlocker множество ключей
     * @return список существующих блокировок
     */
    List<LockData> fetchAllByKeySet(Collection<String> keysBlocker);

    /**
     * Создает несколько блокировок
     *
     * @param lockKeysWithDescription мапа блокировок, где ключом выступает ключ блокировки, а значением -- описание бокировки
     * @param userId                  идентификатор пользователя устанавливающего блокировки
     */
    void lockKeysBatch(Map<String, String> lockKeysWithDescription, int userId);

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
}
