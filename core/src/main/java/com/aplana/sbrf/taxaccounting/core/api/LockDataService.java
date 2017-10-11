package com.aplana.sbrf.taxaccounting.core.api;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

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
	 * Устанавливает блокировки для выполнения асинхронной задачи над объектом
	 * @param key код блокировки
	 * @param userId код установившего блокировку пользователя
	 * @return информация о блокировке
     */
	LockData lockAsync(String key, int userId);

    /**
     * Возвращает данные блокировки по ключу
     * @param key ключ блокировки
     * @return данные блокировки. Если блокировки не существует - возвращается null
     */
    LockData getLock(String key);

    /**
     * Возвращает информацию о всех блокировках, код которых начинающинается c key
     * @param key
     * @return
     */
    List<LockData> getLockStartsWith(String key);

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
     * Получает список всех блокировок
     * @return все блокировки
     * @param filter ограничение по имени пользователя или ключу
     * @param pagingParams параметры пэйджинга
     */
    PagingResult<LockData> getLocks(String filter, PagingParams pagingParams);

    /**
     * Удаляет все указанные блокировки
     * @param keys список ключей блокировок
     */
    void unlockAll(List<String> keys);

	/**
	 * Удаляет блокировки, созданные ранее "seconds" секунд назад. Предполагается, что данный метод
	 * будет вызываться только из задачи планировщика
	 * @param seconds "срок годности" блокировки в секундах
	 * @return количество удаленных блокировок
	 */
	int unlockIfOlderThan(long seconds);

	/**
	 * Удаляет все блокировки, связанные с асинхронной задачей
	 * @param taskId идентификатор задачи
	 */
	void unlockAllByTask(long taskId);

	/**
	 * Связывает блокировку с асинхронной задачей
	 * @param lockKey ключ блокировки
	 * @param taskId идентификатор задачи
	 */
	void bindTask(String lockKey, long taskId);
}
