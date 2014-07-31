package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LockData;

import java.util.Date;

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

}
