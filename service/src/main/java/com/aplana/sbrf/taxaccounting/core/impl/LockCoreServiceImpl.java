package com.aplana.sbrf.taxaccounting.core.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.ObjectLock;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;

@Service
@Transactional
public class LockCoreServiceImpl implements LockCoreService{
	
	@Autowired
	private ObjectLockDao lockDao;

	@Override
	public <T extends Number> void lock(
			Class<? extends IdentityObject<T>> clazz, T id,
			TAUserInfo userInfo) {
		lockDao.lockObject(id, clazz, userInfo.getUser().getId());
	}

	@Override
	public <T extends Number> void unlock(
			Class<? extends IdentityObject<T>> clazz, T id,
			TAUserInfo userInfo) {
		lockDao.unlockObject(id, clazz, userInfo.getUser().getId());
	}

	@Override
	public <T extends Number> void checkLockedMe(
			Class<? extends IdentityObject<T>> clazz, T id,
			TAUserInfo userInfo) {
		if (lockDao.isLockedByUser(id, clazz, userInfo.getUser().getId())){
			lockDao.refreshLock(id, clazz, userInfo.getUser().getId());
		} else {
			throw new ServiceException("Объект не заблокирован текущим пользователем");
		}
	}

	@Override
	public <T extends Number> void checkUnlocked(
			Class<? extends IdentityObject<T>> clazz, T id,
			TAUserInfo userInfo) {
		if (lockDao.getObjectLock(id, clazz) != null){
			throw new ServiceException("Объект заблокирован для редактирования");
		}
	}

	@Override
	public <T extends Number> void checkNoLockedAnother(
			Class<? extends IdentityObject<T>> clazz, T id, TAUserInfo userInfo) {
		ObjectLock<T> lock = lockDao.getObjectLock(id, clazz);
		if (lock != null && lock.getUserId() != userInfo.getUser().getId()){
			throw new ServiceException("Объект заблокирован для редактирования другим пользователем");
		} 
	}

	@Override
	public <T extends Number> ObjectLock<T> getLock(
			Class<? extends IdentityObject<T>> clazz, T id, TAUserInfo userInfo) {
		return lockDao.getObjectLock(id, clazz);
	}
	
}
