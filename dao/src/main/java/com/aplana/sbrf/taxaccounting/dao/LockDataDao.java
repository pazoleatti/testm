package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.PagingResult;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 14:36
 */

public interface LockDataDao {

	/**
	 * Возвращает информацию о блокировке
	 * @param key код блокировки
	 * @return возвращает null, если блокировка по данному коду не найдена
	 */
	LockData get(String key);

    /**
     * Возвращает информацию о блокировке
     * @param key код блокировки
     * @param dateBefore срок жизни блокировки
     * @return возвращает null, если блокировка по данному коду не найдена
     */
    LockData get(String key, Date dateBefore);

	/**
	 * Создает новую блокировку
	 * @param key код блокировки
	 * @param userId код пользователя, установившего блокировку
	 * @param dateBefore срок жизни блокировки
	 */
	void createLock(String key, int userId, Date dateBefore);

	/**
	 * Обновляет блокировку
	 * @param key код блокировки
	 * @param dateBefore срок жизни блокировки
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.LockException если блокировки нет в БД
	 */
	void updateLock(String key, Date dateBefore);

	/**
	 * Снимает блокировку
	 * @param key код блокировки
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.LockException если блокировки нет в БД
	 */
	void deleteLock(String key);

    /**
     * Убрать все блокировки пользователя.
     *
     * @param userId идентификатор пользователя
     * @param ignoreError признак игнорирования ошибок при снятии блокировок. Нужен при разлогинивании
     */
    void unlockAllByUserId(int userId, boolean ignoreError);

    /**
     * Удаляет все блокировки, которые старше заданого времени
     * @param sec
     */
    void unlockIfOlderThan(int sec);

    /**
     * Возвращает список идентификаторов пользователей, которые ожидают разблокировки указанного объекта
     * @param key ключ заблокированного объекта
     * @return список идентификаторов пользователей
     */
    List<Integer> getUsersWaitingForLock(String key);

    /**
     * Добавляет пользователя в список ожидающих выполнения операций над объектом блокировки
     * @param key ключ блокировки
     * @param userId идентификатор пользователя
     */
    void addUserWaitingForLock(String key, int userId);

    /**
     * Получает таймаут для блокировки
     * @param lockObject объект блокировки
     * @return время в милисекундах
     */
    int getLockTimeout(LockData.LockObjects lockObject);

    /**
     * Получает список всех блокировок
     * @return все блокировки
     * @param filter ограничение по имени пользователя или ключу
     * @param startIndex с
     * @param countOfRecords по
     * @param searchOrdering поле по которому выполняется сортировка
     * @param ascSorting порядок сортировки
     */
    PagingResult<LockData> getLocks(String filter, int startIndex, int countOfRecords, LockSearchOrdering searchOrdering, boolean ascSorting);

    /**
     * Удаляет все указанные блокировки
     * @param keys список ключей блокировок
     */
    void unlockAll(List<String> keys);

    /**
     * Продляет все указанные блокировки
     * @param keys список ключей блокировок
     * @param hours количество часов, на которое будут продлены блокировки
     */
    void extendAll(List<String> keys, int hours);
}
