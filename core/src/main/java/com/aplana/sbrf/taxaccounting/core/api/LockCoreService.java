package com.aplana.sbrf.taxaccounting.core.api;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.ObjectLock;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

/**
 * @author sgoryachkin
 *
 */
public interface LockCoreService {
	
	/**
	 * Проверяет, заблокирован ли объект текущим пользователем. 
	 * Если да, то продляет блокировку
	 * 
	 * @param clazz
	 * @param id
	 * @param userInfo
	 */
	<T extends Number> void checkLockedMe(Class<? extends IdentityObject<T>> clazz, T id, TAUserInfo userInfo);
	
	/**
	 * Проверяет что объект разблокирован
	 * 
	 * @param clazz
	 * @param id
	 * @param userInfo
	 */
	<T extends Number> void checkUnlocked(Class<? extends IdentityObject<T>> clazz, T id, TAUserInfo userInfo);
	
	/**
	 * Проверяет что обьект не заблокирован кем то другим
	 * 
	 * @param clazz
	 * @param id
	 * @param userInfo
	 */
	<T extends Number> void checkNoLockedAnother(Class<? extends IdentityObject<T>> clazz, T id, TAUserInfo userInfo);
	
	/**
	 * Устанавливает блокировку по возможности
	 * 
	 * @param clazz
	 * @param id
	 * @param userInfo
	 */
	<T extends Number> void lock(Class<? extends IdentityObject<T>> clazz, T id, TAUserInfo userInfo);
	
	/**
	 * Убирает блокировку по возможности
	 * 
	 * @param clazz
	 * @param id
	 * @param userInfo
	 */
	<T extends Number> void unlock(Class<? extends IdentityObject<T>> clazz, T id, TAUserInfo userInfo);
	
	
	
	<T extends Number> ObjectLock<T> getLock(Class<? extends IdentityObject<T>> clazz, T id, TAUserInfo userInfo);
	
}
