package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.ObjectLock;

/**
 * Dao для установки блокировок на объекты системы. Блокировки предназначены для того, чтобы избежать потери
 * данных, при одновременном редактировании одного и того же объекта разными пользователями.
 * 
 * Блокировать можно только объекты, отнаследованные от {@link IdentityObject} (так как для блокировки необходимо 
 * получить идентификатор объекта)
 * 
 * Общий подход должен быть следующим: перед любым изменением объекта пользователь должен его заблокировать, после окончания
 * работы - разблокировать. 
 */
public interface ObjectLockDao {
	/**
	 * Получить информацию о блокировке объекта
	 * @param id идентификатор объекта
	 * @param clazz тип объекта
	 * @return объект, представляющий {@link ObjectLock информацию о блокировке} или null, если объект не заблокирован. 
	 * В случае, если объект блокировался, но время блокировки истекло, всё равно будет возвращён непустой объект 
	 */
	<IdType extends Number> ObjectLock<IdType> getObjectLock(IdType id, Class<? extends IdentityObject<IdType>> clazz);

	/**
	 * Установить блокировку на объект
	 * @param id идентификатор объекта
	 * @param clazz тип объекта
	 * @param userId идентификатор пользователя, устанавливающего блокировку
	 * @throws LockException если объект заблокирован другим пользователем и время блокировки еще не истекло. 
	 */
	<IdType extends Number> void lockObject(IdType id, Class<? extends IdentityObject<IdType>> clazz, int userId);

	/**
	 * Снять блокировку с объекта
	 * @param id идентификатор объекта
	 * @param clazz тип объекта
	 * @param userId идентификатор пользователя, снимающего блокировку
	 * @throws LockException если объект не заблокирован, или заблокирован другим пользователем. Истёкшие по тайм-ауту блокировки, созданные 
	 * текущим пользователем снимаются без выбрасывания исключения
	 */	
	<IdType extends Number> void unlockObject(IdType id, Class<? extends IdentityObject<IdType>> clazz, int userId);

	/**
	 * Разблокировать все формы, заблокированые пользователем
	 * @param userId ид пользователя
	 */
	public void unlockAllObjectByUserId(int userId);

	/**
	 * Обновляет блокировку на объекте системы (чтобы предотвратить "просрочку" блокировки
	 * @param id идентификатор объекта
	 * @param clazz тип объекта
	 * @param userId идентификатор пользователя, продлевающего блокировку
	 * @throws LockException если объект не заблокирован, данным пользователем
	 */
	<IdType extends Number> void refreshLock(IdType id, Class<? extends IdentityObject<IdType>> clazz, int userId);

	/**
	 * Проверяет, что пользователь имеет действующую блокировку на объекте
	 * @param id идентификатор объекта
	 * @param clazz тип объекта
	 * @param userId идентификатор пользователя, продлевающего блокировку
	 * @return true, если на объекте есть действующая (непросроченная) блокировка сделанная пользователем userId, false - в противном случае
	 */
	<IdType extends Number> boolean isLockedByUser(IdType id, Class<? extends IdentityObject<IdType>> clazz, int userId);
}
