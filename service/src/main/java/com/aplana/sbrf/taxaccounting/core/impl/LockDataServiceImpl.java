package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 17:32
 */

public class LockDataServiceImpl implements LockDataService {

	private static final long WAIT_STEP = 500; //шаг времени между проверками освобождения блокировки, миллисекунды

	@Autowired
	private LockDataDao dao;

	@Override
	public LockData lock(String key, long userId, long age) {
		try {
			synchronized(this) {
				LockData lock = validateLock(dao.get(key));
				if (lock != null) {
					return lock;
				}
				internalLock(key, userId, age);
			}
			return null;
		} catch (Exception e) {
			throw new ServiceException("Не удалось установить блокировку объекта", e);
		}
	}

	@Override
	public void lockWait(String key, long userId, long age, long timeout) {
		long startTime = new Date().getTime();
		while (lock(key, userId, age) != null) {
			try {
				Thread.currentThread().wait(WAIT_STEP);
			} catch (InterruptedException e) {
			}
			if (Math.abs(new Date().getTime() - startTime) > timeout) {
				throw new ServiceException(String.format("Время ожидания (%s мс) для установки блокировки истекло", timeout));
			}
		}
	}

	@Override
	public void unlock(String key, long userId) {
		try {
			synchronized(this) {
				LockData lock = validateLock(dao.get(key));
				if (lock != null) {
					if (lock.getUserId() != userId) {
						throw new ServiceException(String.format("Невозможно удалить блокировку, так как она установлена другим " +
								"пользователем (id = %s). Текущий пользователь id = %s", lock.getUserId(), userId));
					}
					dao.deleteLock(key);
				} else {
					throw new ServiceException(String.format("Нельзя снять несуществующую блокировку. key = \"%s\"", key));
				}
			}
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException("Не удалось снять блокировку с объекта", e);
		}
	}

	@Override
	public void extend(String key, long userId, long age) {
		try {
			synchronized(this) {
				LockData lock = validateLock(dao.get(key));
				if (lock != null) {
					if (lock.getUserId() != userId) {
						throw new ServiceException(String.format("Невозможно продлить блокировку, так как она установлена другим " +
							"пользователем (id = %s). Текущий пользователь id = %s", lock.getUserId(), userId));
					}
					Date dateBefore = new Date();
					dao.updateLock(key, new Date(dateBefore.getTime() + age));
				} else {
					internalLock(key, userId, age); // создаем блокировку, если ее не было
				}
			}
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException("Не удалось продлить блокировку объекта", e);
		}
	}

	/**
	 * Проверяет блокировку. Если срок ее действия вышел, то она удаляется
	 */
	private LockData validateLock(LockData lock) {
		if (lock == null) {
			return null;
		}
		if (new Date().after(lock.getDateBefore())) {
			dao.deleteLock(lock.getKey());
			return null;
		}
		return lock;
	}

	/**
	 * Блокировка без всяких проверок - позволяет сократить количество обращений к бд для вложенных вызовов методов
	 */
	private void internalLock(String key, long userId, long age) {
		Date dateBefore = new Date();
		dao.createLock(key, userId, new Date(dateBefore.getTime() + age));
	}
}