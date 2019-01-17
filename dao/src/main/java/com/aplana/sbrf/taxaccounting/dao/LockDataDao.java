package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockDataDTO;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 14:36
 */

public interface LockDataDao extends PermissionDao {

    /**
     * Возвращает информацию о блокировке
     *
     * @param key  код блокировки
     * @param like проверяем неполное совпадение ключа?
     * @return возвращает null, если блокировка по данному коду не найдена
     */
    LockData get(String key, boolean like);

    /**
     * Возвращает информацию о блокировке
     *
     * @param key      код блокировки
     * @param lockDate дата начала блокировки
     * @return возвращает null, если блокировка по данному коду не найдена
     */
    LockData get(String key, Date lockDate);

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
    void unlockAllByTask(long taskId);

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
    List<LockData> fetchAllByKeySet(Set<String> keysBlocker);
}
