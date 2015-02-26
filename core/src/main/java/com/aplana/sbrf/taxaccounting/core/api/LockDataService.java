package com.aplana.sbrf.taxaccounting.core.api;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockSearchOrdering;
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
	 * Устанавливает новую блокировку до времени = now + age. Если блокировка успешно установилась, то возвращается null.
	 * Если блокировка уже существовала, то возвращется информация по этой блокировке в виде объекта LockData
     * @param key код блокировки
	 * @param userId код установившего блокировку пользователя
	 * @param age относительное время жизни блокировки в миллисекундах
	 * @return информация о блокировке
	 */
	LockData lock(String key, int userId, long age);

    /**
     * Возвращает данные блокировки по ключу
     * @param key ключ блокировки
     * @return данные блокировки. Если блокировки не существует - возвращается null
     */
    LockData getLock(String key);

    /**
	 * Установка блокировки с ожиданием
	 * В течении времени timeout ожидает пока объект не будет разблокирован. При его освобождении тут же блокирует и
	 * возвращает управление вызвавшему методу. Если за время timeout объект не удалось перехватить, то вызывается exception
	 *
	 * @param key код блокировки
	 * @param userId код установившего блокировку пользователя
	 * @param age относительное время жизни блокировки в миллисекундах
	 * @param timeout максимальное относительное время ожидания для установки новой блокировки
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если время ожидания timeout истекло
	 */
	void lockWait(String key, int userId, long age, long timeout);

	/**
	 * Снимает блокировку по ее идентификатору. Если блокировки не было, либо была установлена другим пользователем, то exception.
	 *
	 * @param key код блокировки
	 * @param userId код установившего блокировку пользователя
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если блокировка была установлена другим пользователем, либо блокировки не было в бд
	 */
	void unlock(String key, int userId);

    /**
     * Снимает блокировку по ее идентификатору. Если блокировки не было, либо была установлена другим пользователем, то exception.
     *
     * @param key код блокировки
     * @param userId код установившего блокировку пользователя
     * @param force при force = true блокировка снимается принудительно, без вывода ошибки при отсутсвии блокировки
     * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если блокировка была установлена другим пользователем, либо блокировки не было в бд
     */
    void unlock(String key, int userId, boolean force);

    /**
	 * Аналогично методу lock с той разницей, что если блокировка объекта от имени указанного пользователя существует,
	 * то она продлевается по времени (now + age )
	 *
	 * @param key код блокировки
	 * @param userId код установившего блокировку пользователя
	 * @param age относительное время жизни блокировки в миллисекундах
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException если блокировка была установлена другим пользователем
	 */
	void extend(String key, int userId, long age);

    void unlockAll(TAUserInfo userInfo);

    /**
     * Убрать все блокировки пользователя.
     *
     * @param userInfo пользователь
     * @param ignoreError признак игнорирования ошибок при снятии блокировок. Нужен при разлогинивании
     */
    void unlockAll(TAUserInfo userInfo, boolean ignoreError);

    void unlockIfOlderThan(int sec);

    /**
     * Проверяет, установлена ли блокировка на указанном объекте
     * @param key код блокировки
     * @return блокировка установлена?
     */
    boolean isLockExists(String key);

    /**
     * Проверяет, установлена ли блокировка на указанном объекте с определенной датой окончания действия блокировки
     * @param key код блокировки
     * @param lockDateEnd дата окончания действия блокировки
     * @return блокировка установлена?
     */
    boolean isLockExists(String key, Date lockDateEnd);

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
