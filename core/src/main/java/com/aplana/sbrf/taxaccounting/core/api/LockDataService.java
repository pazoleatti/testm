package com.aplana.sbrf.taxaccounting.core.api;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Date;
import java.util.List;

/**
 <h1>Сервис блокировок</h1>
 <p>Порядок работы с блокировками:</p>
 полная синхронизация даже в рамках одного пользователя:
 <ol>
	 <li>Пытаемся установить блокировку на объект - метод "lockWait"</li>
	 <li>Выполняем операции над объектом</li>
	 <li>Если операция длительная, то для продления блокировки вызываем "extend"</li>
	 <li>После завершения работы над объектом вызываем метод "unlock"</li>
 </ol>
 обычное разграничение объектов (НФ, деклараций) между пользователями:
 <ol>
 	<li>Пытаемся установить блокировку
		<ol>
			<li>Удалось установить блокировку
				<ol>
					 <li>Выполняем операции над объектом</li>
					 <li>Если операция длительная, то периодически вызываем метод "extend"</li>
					 <li>После завершения работы над объектом вызываем метод "unlock"</li>
				</ol>
			</li>
			<li>Не удалось установить блокировку
				<ol>
					<li>Отображаем пользователю информацию о блокировке - кто и до какого времени заблокировал</li>
				</ol>
			</li>
		</ol>
 	</li>
 </ol>

 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 13:50
 */

public interface LockDataService {

    String LOCK_DATA = "Объект заблокирован для редактирования пользователем \"%s\"(id=%s)";

	/**
	 * Устанавливает новую блокировку. Если блокировка успешно установилась, то возвращается null.
	 * Если блокировка уже существовала, то возвращется информация по этой блокировке в виде объекта LockData
     * @param key код блокировки
	 * @param userId код установившего блокировку пользователя
     * @param description описание блокировки
	 * @return информация о блокировке
	 */
	LockData lock(String key, int userId, String description);

    /**
     * Устанавливает новую блокировку. Если блокировка успешно установилась, то возвращается null.
     * Если блокировка уже существовала, то возвращется информация по этой блокировке в виде объекта LockData
     * @param key код блокировки
     * @param userId код установившего блокировку пользователя
     * @param description описание блокировки
     * @param state Статус асинхронной задачи, связанной с блокировкой
     * @return информация о блокировке
     */
	LockData lock(String key, int userId, String description, String state);

    /**
     * Возвращает данные блокировки по ключу
     * @param key ключ блокировки
     * @return данные блокировки. Если блокировки не существует - возвращается null
     */
    LockData getLock(String key);

	/**
	 * Снимает блокировку по ее идентификатору. Если блокировки не было, либо была установлена другим пользователем, то exception.
	 *
	 * @param key код блокировки
	 * @param userId код установившего блокировку пользователя
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если блокировка была установлена другим пользователем, либо блокировки не было в бд
	 */
    Boolean unlock(String key, int userId);

    /**
     * Снимает блокировку по ее идентификатору. Если блокировки не было, либо была установлена другим пользователем, то exception.
     *
     * @param key код блокировки
     * @param userId код установившего блокировку пользователя
     * @param force при force = true блокировка снимается принудительно, без вывода ошибки при отсутсвии блокировки
     * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если блокировка была установлена другим пользователем, либо блокировки не было в бд
     */
    Boolean unlock(String key, int userId, boolean force);

    void unlockAll(TAUserInfo userInfo);

    /**
     * Убрать все блокировки пользователя.
     *
     * @param userInfo пользователь
     * @param ignoreError признак игнорирования ошибок при снятии блокировок. Нужен при разлогинивании
     */
    void unlockAll(TAUserInfo userInfo, boolean ignoreError);

    /**
     * Проверяет, установлена ли блокировка на указанном объекте
     * @param key код блокировки
     * @param like проверяем неполное совпадение ключа?
     * @return блокировка установлена?
     */
    boolean isLockExists(String key, boolean like);

    /**
     * Проверяет, установлена ли блокировка на указанном объекте с определенной датой окончания действия блокировки
     * @param key код блокировки
     * @param lockDate дата начала действия блокировки
     * @return блокировка установлена?
     */
    boolean isLockExists(String key, Date lockDate);

    /**
     * Добавляет пользователя в список ожидающих выполнения операций над объектом блокировки
     * @param key ключ блокировки
     * @param userId идентификатор пользователя
     */
    void addUserWaitingForLock(String key, int userId);

    /**
     * Возвращает список идентификаторов пользователей, которые ожидают разблокировки указанного объекта
     * @param key ключ заблокированного объекта
     * @return список идентификаторов пользователей
     */
    List<Integer> getUsersWaitingForLock(String key);

    /**
     * Получает список всех блокировок
     * @return все блокировки
     * @param filter ограничение по имени пользователя или ключу
     * @param queues тип очереди
     * @param pagingParams параметры пэйджинга
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
     */
    void updateState(String key, Date lockDate, String state);

    /**
     * Обновляет очередь, к которой относится асинхронная задача, связанная с указанной блокировкой
     * @param key код блокировки
     * @param lockDate дата начала действия блокировки
     * @param queue очередь
     */
    void updateQueue(String key, Date lockDate, LockData.LockQueues queue);

	/**
	 * Удаляет блокировки, созданные ранее "seconds" секунд назад. Предполагается, что данный метод
	 * будет вызываться только из задачи планировщика
	 * @param seconds "срок годности" блокировки в секундах
	 * @return количество удаленных блокировок
	 */
	int unlockIfOlderThan(long seconds);

    /**
     * Останавливает выполнение задач с указанными ключами блокировки, удаляет блокировку и
     * отправляет оповещения ожидающим блокировку пользователям
     * @param lockData блокировка
     * @param userId идентификатор пользователя, отменяющего блокировку
     * @param force признак принудительного снятия блокировки
     */
    void interruptTask(LockData lockData, int userId, boolean force, String cause);

    /**
     * Останавливает выполнение задач с указанными ключами блокировки, удаляет блокировку и
     * отправляет оповещения ожидающим блокировку пользователям
     * @param lockKeys ключи блокировок на удаление
     * @param userId идентификатор пользователя, отменяющего блокировку
     */
    void interruptAllTasks(List<String> lockKeys, int userId, String cause);

    /**
     * Выводит в logger информации о блокировке
     * @param lockData
     * @param logger
     */
    void lockInfo(LockData lockData, Logger logger);
}
