package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 17:32
 */

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class LockDataServiceImpl implements LockDataService {

	private static final long SLEEP_TIME = 500; //шаг времени между проверками освобождения блокировки, миллисекунды

	@Autowired
	private LockDataDao dao;

	@Autowired
	private TAUserDao userDao;

    @Autowired
    private TransactionHelper tx;

	@Override
	public LockData lock(final String key, final int userId, final long age) {
        return tx.returnInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData executeWithReturn() {
                try {
                    synchronized(LockDataServiceImpl.class) {
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
            public void execute() {}
        });
	}

	@Override
	public void lockWait(final String key, final int userId, final long age, final long timeout) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                long startTime = new Date().getTime();
                while (lock(key, userId, age) != null) {
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                    }
                    if (Math.abs(new Date().getTime() - startTime) > timeout) {
                        throw new ServiceException(String.format("Время ожидания (%s мс) для установки блокировки истекло", timeout));
                    }
                }
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
	}

	@Override
	public void unlock(final String key, final int userId) {
        unlock(key, userId, false);
    }

    @Override
    public void unlock(final String key, final int userId, final boolean force) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = validateLock(dao.get(key));
                        if (lock != null) {
                            if (!force && lock.getUserId() != userId) {
                                TAUser blocker = userDao.getUser(lock.getUserId());
                                throw new ServiceException(String.format("Невозможно удалить блокировку, так как она установлена " +
                                        "пользователем \"%s\"(%s).", blocker.getLogin(), blocker.getId()));
                            }
                            dao.deleteLock(key);
                        } else if (!force) {
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
            public Object executeWithReturn() {
                return null;
            }
        });
	}

	@Override
	public void extend(final String key, final int userId, final long age) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = validateLock(dao.get(key));
                        if (lock != null) {
                            if (lock.getUserId() != userId) {
                                TAUser blocker = userDao.getUser(lock.getUserId());
                                throw new ServiceException(String.format("Невозможно продлить блокировку, так как она установлена " +
                                        "пользователем \"$s\"(id = %s). Текущий пользователь id = %s", blocker.getLogin(), blocker.getId()));
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

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
	}

    @Override
    public void unlockAll(TAUserInfo userInfo) {
        dao.unlockAllByUserId(userInfo.getUser().getId());
    }

    @Override
    public void unlockIfOlderThan(int sec) {
        dao.unlockIfOlderThan(sec);
    }

    @Override
    public boolean isLockExists(final String key) {
        synchronized(LockDataServiceImpl.class) {
            return validateLock(dao.get(key)) != null;
        }
    }

    @Override
    public void addUserWaitingForLock(final String key, final int userId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = validateLock(dao.get(key));
                        if (lock != null) {
                            dao.addUserWaitingForLock(key, userId);
                        } else {
                            throw new ServiceException(String.format("Нельзя ожидать несуществующий объект блокировки. key = \"%s\"", key));
                        }
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось добавить пользователя в список ожидающих объект блокировки", e);
                }
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
    }

    @Override
    public List<Integer> getUsersWaitingForLock(String key) {
        synchronized(LockDataServiceImpl.class) {
            try {
                return dao.getUsersWaitingForLock(key);
            } catch (Exception e) {
                throw new ServiceException("Не удалось получить список пользователей ожидающих объект блокировки", e);
            }
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
	private void internalLock(String key, int userId, long age) {
		Date dateBefore = new Date();
		dao.createLock(key, userId, new Date(dateBefore.getTime() + age));
	}
}