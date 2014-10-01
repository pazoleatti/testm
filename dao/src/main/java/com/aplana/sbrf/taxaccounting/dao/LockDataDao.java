package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LockData;

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

    void unlockAllByUserId(int userId);

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
}
