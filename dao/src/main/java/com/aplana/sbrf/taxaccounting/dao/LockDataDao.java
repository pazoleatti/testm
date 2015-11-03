package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
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
    LockData get(String key, Date lockDate);

	/**
	 * Создает новую блокировку
     * @param key код блокировки
     * @param userId код пользователя, установившего блокировку
     * @param description описание блокировки
     * @param state Статус асинхронной задачи, связанной с блокировкой
     * @param serverNode Наименование узла кластера, на котором выполняется связанная асинхронная задача
     */
	void lock(String key, int userId, String description, String state, String serverNode);

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
     * Получает список всех блокировок с учетом фильтра + пейджинг. Используется на форме просмотра блокировок.
     * @return все блокировки
     * @param filter ограничение по имени пользователя или ключу. Необязательный параметр. Может быть null
     * @param queues тип очереди. Необязательный параметр. По умолчанию LockQueues.ALL. Может быть null
     * @param pagingParams параметры пэйджинга. Обязательный параметр
     */
    PagingResult<LockData> getLocks(String filter, LockData.LockQueues queues, PagingParams pagingParams);

    /**
     * Удаляет все указанные блокировки
     * @param keys список ключей блокировок
     */
    void unlockAll(List<String> keys);

    /**
     * Обновляет статус выполнения асинхронной задачи, связанной с блокировкой
     * @param key код блокировки
     * @param lockDate дата начала действия блокировки
     * @param state новый статус
     * @param serverNode Наименование узла кластера, на котором выполняется связанная асинхронная задача
     */
    void updateState(String key, Date lockDate, String state, String serverNode);

    /**
     * Обновляет очередь, к которой относится асинхронная задача, связанная с указанной блокировкой
     * @param key код блокировки
     * @param lockDate дата начала действия блокировки
     * @param queue очередь
     */
    void updateQueue(String key, Date lockDate, LockData.LockQueues queue);

	/**
	 * Удаляет блокировки, созданные ранее "seconds" секунд назад
	 * @param seconds "срок годности" блокировки в секундах
	 * @return количество удаленных блокировок
	 */
	int unlockIfOlderThan(long seconds);
}
