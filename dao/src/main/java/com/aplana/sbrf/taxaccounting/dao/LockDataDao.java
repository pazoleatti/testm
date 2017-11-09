package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 14:36
 */

public interface LockDataDao extends PermissionDao {

    /**
     * Возвращает информацию о блокировке
     * @param key код блокировки
     * @param like проверяем неполное совпадение ключа?
     * @return возвращает null, если блокировка по данному коду не найдена
     */
    LockData get(String key, boolean like);

    /**
     * Возвращает информацию о блокировке
     * @param key код блокировки
     * @param lockDate дата начала блокировки
     * @return возвращает null, если блокировка по данному коду не найдена
     */
    LockData get(String key, LocalDateTime lockDate);

	/**
	 * Создает новую блокировку
     * @param key код блокировки
     * @param userId код пользователя, установившего блокировку
     * @param description описание блокировки
     */
	void lock(String key, int userId, String description);

    /**
     * Создает новую блокировку без описания. Используется для создания блокировок для асинхронных задач для упрощения кода,
     * т.к в этом случае описание хранится в самой асинхронной задаче
     * @param key код блокировки
     * @param userId код пользователя, установившего блокировку
     */
	void lock(String key, int userId);

	/**
	 * Снимает блокировку
	 * @param key код блокировки
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.LockException если блокировки нет в БД
	 */
	void unlock(String key);

    /**
     * Убрать все блокировки пользователя.
     *
     * @param userId идентификатор пользователя
     * @param ignoreError признак игнорирования ошибок при снятии блокировок. Нужен при разлогинивании
     */
    void unlockAllByUserId(int userId, boolean ignoreError);

    /**
     * Удаляет все блокировки, связанные с асинхронной задачей
     * @param taskId идентификатор задачи
     */
    void unlockAllByTask(long taskId);

    /**
     * Получает список всех блокировок с учетом фильтра + пейджинг. Используется на форме просмотра блокировок.
     * @return все блокировки
     * @param filter ограничение по имени пользователя или ключу. Необязательный параметр. Может быть null
     * @param pagingParams параметры пэйджинга. Обязательный параметр
     */
    PagingResult<LockData> getLocks(String filter, PagingParams pagingParams);

    /**
     * Удаляет все указанные блокировки
     * @param keys список ключей блокировок
     */
    void unlockAll(List<Long> keys);

    /**
     * Возвращяет список истекших блокировок
     * @param seconds "срок годности" блокировки в секундах
     * @return список истекший блокировок
     */
    List<String> getLockIfOlderThan(long seconds);

    /**
     * Связывает блокировку с асинхронной задачей
     * @param lockKey ключ блокировки
     * @param taskId идентификатор задачи
     */
    void bindTask(String lockKey, long taskId);
}
