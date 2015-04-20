package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static final Log log = LogFactory.getLog(LockDataServiceImpl.class);

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
                        LockData lock = validateLock(dao.get(key, false));
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
    public LockData getLock(String key) {
        synchronized(LockDataServiceImpl.class) {
            return validateLock(dao.get(key, false));
        }
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
                        LockData lock = validateLock(dao.get(key, false));
                        if (lock != null) {
                            if (!force && lock.getUserId() != userId) {
                                TAUser blocker = userDao.getUser(lock.getUserId());
                                throw new ServiceException(String.format("Невозможно удалить блокировку, так как она установлена " +
                                        "пользователем \"%s\"(%s).", blocker.getLogin(), blocker.getId()));
                            }
                            dao.deleteLock(key);
                        } else if (!force) {
                            log.warn(String.format("Нельзя снять несуществующую блокировку. key = \"%s\"", key));
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
                        LockData lock = validateLock(dao.get(key, false));
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
        dao.unlockAllByUserId(userInfo.getUser().getId(), false);
    }

    @Override
    public void unlockAll(TAUserInfo userInfo, boolean ignoreError) {
        dao.unlockAllByUserId(userInfo.getUser().getId(), ignoreError);
    }

    @Override
    public void unlockIfOlderThan(int sec) {
        dao.unlockIfOlderThan(sec);
    }

    @Override
    public boolean isLockExists(final String key, boolean like) {
        synchronized(LockDataServiceImpl.class) {
            return validateLock(dao.get(key, like)) != null;
        }
    }

    @Override
    public boolean isLockExists(String key, Date lockDate) {
        synchronized(LockDataServiceImpl.class) {
            return validateLock(dao.get(key, lockDate)) != null;
        }
    }

    @Override
    public void addUserWaitingForLock(final String key, final int userId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = validateLock(dao.get(key, false));
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

    @Override
    public int getLockTimeout(LockData.LockObjects lockObject) {
        try {
            return dao.getLockTimeout(lockObject);
        } catch (Exception e) {
            throw new ServiceException(String.format("Не удалось получить таймаут для блокировки объекта с ключом = %s",lockObject.name()), e);
        }
    }

    @Override
    public PagingResult<LockData> getLocks(String filter, int startIndex, int countOfRecords,
                                           LockSearchOrdering searchOrdering, boolean ascSorting) {
        return dao.getLocks(filter, startIndex, countOfRecords, searchOrdering, ascSorting);
    }

    @Override
    public void unlockAll(List<String> keys) {
        dao.unlockAll(keys);
    }

    @Override
    public void extendAll(List<String> keys, int hours) {
        dao.extendAll(keys, hours);
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