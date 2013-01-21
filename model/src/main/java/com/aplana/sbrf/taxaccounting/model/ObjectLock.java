package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * Информация о блокировке объекта
 * @author dsultanbekov
 */
public class ObjectLock<IdType extends Number> {
	private long objectId;
	private Class<? extends IdentityObject<IdType>> objectClass;
	private int userId;
	private Date lockTime;	
	
	/**
	 * Получить первичный ключ блокируемого объекта (поддерживаются только целочисленные ключи)	
	 * @return первичный ключ блокируемого объекта
	 */
	public long getObjectId() {
		return objectId;
	}
	
	/**
	 * Задать первичный ключ блокируемого объекта (поддерживаются только целочисленные ключи)
	 * @param objectId первичный ключ блокируемого объекта
	 */
	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}
	
	/**
	 * Получить тип блокируемого объекта
	 * @return тип блокируемого объекта
	 */
	public Class<? extends IdentityObject<IdType>> getObjectClass() {
		return objectClass;
	}
	
	/**
	 * Задать тип блокируемого объекта (должен быть отнаследован от {@link IdentityObject}
	 * @param objectClass тип блокируемого объекта
	 */
	public void setObjectClass(Class<? extends IdentityObject<IdType>> objectClass) {
		this.objectClass = objectClass;
	}
	
	/**
	 * Получить идентификатор пользователя, выполнившего блокировку
	 * @return идентфикатор пользователя, выполнившего блокировку
	 */
	public int getUserId() {
		return userId;
	}
	
	/**
	 * Задать индентификатор пользователя, выполнившего блокировку объекта
	 * @param userId идентфикатор пользователя, выполнившего блокировку
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * Получить время, когда установлена блокировка
	 * @return время блокировки (с точностью до секунд)
	 */
	public Date getLockTime() {
		return lockTime;
	}
	
	/**
	 * Задать время блокировки (обращаю внимаение, что в БД время обрезается до секунд)
	 * @param lockTime время блокировки
	 */
	public void setLockTime(Date lockTime) {
		this.lockTime = lockTime;
	}
}
